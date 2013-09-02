package de.fraunhofer.igd.klarschiff.service.image;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.IOException;

import javax.imageio.ImageIO;

import org.apache.commons.io.output.ByteArrayOutputStream;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import com.sun.image.codec.jpeg.JPEGCodec;
import com.sun.image.codec.jpeg.JPEGEncodeParam;
import com.sun.image.codec.jpeg.JPEGImageEncoder;

import de.fraunhofer.igd.klarschiff.vo.Vorgang;

/**
 * Der Service dient zur Manipulation von Bildern, wie z.B. das Skalieren oder das Ausschwärzen von Bildbereichen, bzw. 
 * zum Auslesen eines Bildes aus einem HTTP-Request
 * @author Stefan Audersch (Fraunhofer IGD)
 * @author Marcus Kröller (Fraunhofer IGD)
 */
@Service
public class ImageService {
	
	/**
	 * Gibt an, wie ein Bild skaliert werden soll. Das Ergebnisbild hat nach der Skalierung die entsprechenden maximalen
	 * Abmaße bei <code>max</code>. Das Ergebnisbild hat nach der Skalierung genau die Abmaße haben soll, wobei entsprechende 
	 * Teile des Bildes transparent gemacht wurden bei <code>correct</code>
	 */
	public enum ScaleTyp { max, correct }

	int fotoNormalWidth = 360;
	int fotoNormalHeight = 360;
	int fotoThumbWidth = 150;
	int fotoThumbHeight = 150;
	ScaleTyp scaleTyp = ScaleTyp.max;
	
	/**
	 * Setzt das Bild für einen Vorgang. Dabei wird das Bild sowohl für die Vorschau als auch das eigentliche Bild abgelegt.
	 * Die Bilder werden entsprechend scaliert
	 * @param image Bild als ByteArray
	 * @param vorgang Vorgang in dem die Bilddaten gesetzt werden sollen
	 * @throws Exception
	 */
	public void setImageForVorgang(byte[] image, Vorgang vorgang) throws Exception {

    	vorgang.setFotoNormalJpg(scaleImage(image, fotoNormalWidth, fotoNormalHeight, scaleTyp));
    	vorgang.setFotoThumbJpg(scaleImage(image, fotoThumbWidth, fotoThumbHeight, scaleTyp));
	}

	/**
	 * Schwarz Bereiche in einem Bild aus. Das Vorschau- als auch das eigentliche Bild eines Vorganges wird dabei geändert.
	 * @param vorgang Vorgang, bei dem Bereiche aus dem Bild geschwarzt werden sollen
	 * @param rectangles Liste von Rechtecken als String, die für die Schwarzung verwendet werden sollen
	 * @param width relative Höhe, die bei der Erstellung der Rechtecke verwendet wurde
	 * @param height relative Breite, die bei der Erstellung der rechtecke verwendet wurde
	 */
	public void censorImageForVorgang(Vorgang vorgang, String rectangles, Integer width, Integer height)
	{
		try {
			//censor normal image
			BufferedImage image = ImageIO.read(new ByteArrayInputStream(vorgang.getFotoNormalJpg()));
			float heightScaling =  (float)image.getHeight() / height.floatValue();
			float widthScaling = (float)image.getWidth() / width.floatValue();
			Graphics2D graphics2D = image.createGraphics();
			graphics2D.setColor(Color.black);
			for (String rectangle : rectangles.split(";")){
				String[] cords = rectangle.split(",");
				graphics2D.fillRect((int)((int)Float.parseFloat(cords[0])*heightScaling),
									(int)((int)Float.parseFloat(cords[1])*widthScaling),
									(int)((int)Float.parseFloat(cords[2])*heightScaling),
									(int)((int)Float.parseFloat(cords[3])*widthScaling));
			}
			graphics2D.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BILINEAR);
			
			graphics2D.drawImage(image, null, null);
			vorgang.setFotoNormalJpg(imageToByteArray(image));
	    	
			//Thumb neu berechnen
			vorgang.setFotoThumbJpg(scaleImage(vorgang.getFotoNormalJpg(), fotoThumbWidth, fotoThumbHeight, scaleTyp));
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}
    
    public void rotateImageForVorgang(Vorgang vorgang)
	{
		try {
            BufferedImage oldImage = ImageIO.read(new ByteArrayInputStream(vorgang.getFotoNormalJpg()));
            BufferedImage newImage = new BufferedImage(oldImage.getHeight(), oldImage.getWidth(), oldImage.getType());
            
            Graphics2D graphics2D = (Graphics2D) newImage.getGraphics();
            graphics2D.rotate(Math.toRadians(90), newImage.getWidth() / 2, newImage.getHeight() / 2);
            graphics2D.translate((newImage.getWidth() - oldImage.getWidth()) / 2, (newImage.getHeight() - oldImage.getHeight()) / 2);
            graphics2D.drawImage(oldImage, 0, 0, oldImage.getWidth(), oldImage.getHeight(), null);

            vorgang.setFotoNormalJpg(imageToByteArray(newImage));
	    	vorgang.setFotoThumbJpg(scaleImage(vorgang.getFotoNormalJpg(), fotoThumbWidth, fotoThumbHeight, scaleTyp));
            
        } catch (Exception e) {
			throw new RuntimeException(e);
		}
	}


	/**
	 * Setzt das Foto (Vorschau und eigentliches Bild) für einen Vorgang.
	 * @param multipartFile Teil des HTTP-Requestes mit den Bilddaten
	 * @param vorgang Vorgang bei dem das foto gesetzt werden soll
	 * @throws Exception
	 */
	public void setImageForVorgang(MultipartFile multipartFile, Vorgang vorgang) throws Exception {
		byte[] image = multipartFile.getBytes();

		setImageForVorgang(image, vorgang);
	}

	
	/**
	 * Scalliert ein Bild. Dabei kann über den ScaleType angegeben werden, ob das Ergebnisbild die entsprechenden maximalen
	 * Abmaße haben soll, oder das Ergebnisbild genau die Abmaße haben soll, wobei entsprechende Teile des Bildes dann transparent
	 * gemacht werden sollen. 
	 * @param image Bild als ByteArray, welches scaliert werden soll
	 * @param width Breite bzw. maximale Breite des Ergebnisbild
	 * @param height Höhe bzw. maximale Höhe des Ergebnisbildes
	 * @param scaleTyp Typ der Transfomation
	 * @return scalliertes Bild als ByteArray
	 * @throws Exception
	 */
	public static byte[] scaleImage(byte[] image, Integer width, Integer height, ScaleTyp scaleTyp) throws Exception 
	{

		BufferedImage sourceImage = ImageIO.read(new ByteArrayInputStream(image)); 
		BufferedImage scaleImage = null;
		
		if (width==null || height==null) {
			scaleImage = sourceImage;
		} else {
			int scaleWidth = width;
			int scaleHeight = height;        
	 
			// Make sure the aspect ratio is maintained, so the image is not skewed
			double scaleRatio = (double)scaleWidth / (double)scaleHeight;
			int sourceImageWidth = sourceImage.getWidth(null);
			int sourceImageHeight = sourceImage.getHeight(null);
			double imageRatio = (double)sourceImageWidth / (double)sourceImageHeight;
			if (scaleRatio < imageRatio) scaleHeight = (int)(scaleWidth / imageRatio);
			else scaleWidth = (int)(scaleHeight * imageRatio);
	
	        // Draw the scaled image
			switch (scaleTyp) {
			case max:
				scaleImage = new BufferedImage(scaleWidth, scaleHeight, BufferedImage.TYPE_INT_RGB);
				break;
			case correct:
				scaleImage = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);
				break;
			}
			Graphics2D graphics2D = scaleImage.createGraphics();
			graphics2D.setBackground(Color.white);
			graphics2D.clearRect(0, 0, width, height);
			graphics2D.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BILINEAR);
			switch (scaleTyp) {
			case max:
				graphics2D.drawImage(sourceImage, 0, 0, scaleWidth, scaleHeight, null);
				break;
			case correct:
				graphics2D.drawImage(sourceImage, (width-scaleWidth)/2, (height-scaleHeight)/2, scaleWidth, scaleHeight, null);
				break;
			}
		}
		
        // Write the scaled image to the outputstream
		return imageToByteArray(scaleImage);
	}

	
	/**
	 * Erzeugt aus einem Bild als BufferedImage ein ByteArray (JPEG)
	 * @param image Bild, welches in ein ByteArray transformiert werden soll
	 * @return ByteArray (JPEG) des Bildes
	 * @throws IOException
	 */
	private static byte[] imageToByteArray(BufferedImage image) throws IOException {
		ByteArrayOutputStream bos = new ByteArrayOutputStream();
		JPEGImageEncoder encoder = JPEGCodec.createJPEGEncoder(bos);
		JPEGEncodeParam param = encoder.getDefaultJPEGEncodeParam(image);
        param.setQuality(0.8f, false);
        encoder.setJPEGEncodeParam(param);
        encoder.encode(image);        
        ImageIO.write(image, "jpg" , bos); 
 
        return bos.toByteArray();
	}

	/* --------------- GET + SET ----------------------------*/
	
	public int getFotoNormalWidth() {
		return fotoNormalWidth;
	}

	public void setFotoNormalWidth(int fotoNormalWidth) {
		this.fotoNormalWidth = fotoNormalWidth;
	}

	public int getFotoNormalHeight() {
		return fotoNormalHeight;
	}

	public void setFotoNormalHeight(int fotoNormalHeight) {
		this.fotoNormalHeight = fotoNormalHeight;
	}

	public int getFotoThumbWidth() {
		return fotoThumbWidth;
	}

	public void setFotoThumbWidth(int fotoThumbWidth) {
		this.fotoThumbWidth = fotoThumbWidth;
	}

	public int getFotoThumbHeight() {
		return fotoThumbHeight;
	}

	public void setFotoThumbHeight(int fotoThumbHeight) {
		this.fotoThumbHeight = fotoThumbHeight;
	}

	public ScaleTyp getScaleTyp() {
		return scaleTyp;
	}

	public void setScaleTyp(ScaleTyp scaleTyp) {
		this.scaleTyp = scaleTyp;
	}

}
