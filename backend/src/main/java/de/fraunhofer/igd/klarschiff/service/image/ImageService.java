package de.fraunhofer.igd.klarschiff.service.image;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.IOException;

import javax.imageio.ImageIO;
import javax.imageio.plugins.jpeg.JPEGImageWriteParam;

import org.apache.commons.io.output.ByteArrayOutputStream;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import com.sun.imageio.plugins.jpeg.JPEGImageWriter;
import de.fraunhofer.igd.klarschiff.service.settings.SettingsService;

import de.fraunhofer.igd.klarschiff.vo.Vorgang;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.UUID;
import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * Der Service dient zur Manipulation von Bildern, wie z.B. das Skalieren oder das Ausschwärzen von Bildbereichen, bzw. 
 * zum Auslesen eines Bildes aus einem HTTP-Request
 * @author Stefan Audersch (Fraunhofer IGD)
 * @author Marcus Kröller (Fraunhofer IGD)
 * @author Alexander Kruth (BFPI)
 */
@Service
public class ImageService {
    @Autowired
	SettingsService settingsService;

	/**
	 * Gibt an, wie ein Bild skaliert werden soll. Das Ergebnisbild hat nach der Skalierung die entsprechenden maximalen
	 * Abmaße bei <code>max</code>. Das Ergebnisbild hat nach der Skalierung genau die Abmaße haben soll, wobei entsprechende 
	 * Teile des Bildes transparent gemacht wurden bei <code>correct</code>
	 */
	public enum ScaleTyp { max, correct }

	int fotoGrossWidth = 720;
	int fotoGrossHeight = 720;
	int fotoNormalWidth = 360;
	int fotoNormalHeight = 360;
	int fotoThumbWidth = 150;
	int fotoThumbHeight = 150;
	ScaleTyp scaleTyp = ScaleTyp.max;
	
	/**
	 * Setzt das Bild für einen Vorgang. Dabei wird das Bild in drei Größen
	 * abgelegt (Gross, Normal, Thumb).
	 * Die Bilder werden entsprechend skaliert.
	 * @param image Bild als ByteArray
	 * @param vorgang Vorgang in dem die Bilddaten gesetzt werden sollen
	 * @throws Exception
	 */
	public void setImageForVorgang(byte[] image, Vorgang vorgang) throws Exception {
      vorgang.setFotoGross(generateFilenameAndWriteFile(
              scaleImage(image, fotoGrossWidth, fotoGrossHeight, scaleTyp),
              vorgang, vorgang.getFotoGross(), "gross"));
      vorgang.setFotoNormal(generateFilenameAndWriteFile(
              scaleImage(image, fotoNormalWidth, fotoNormalHeight, scaleTyp),
              vorgang, vorgang.getFotoNormal(), "normal"));
      vorgang.setFotoThumb(generateFilenameAndWriteFile(
              scaleImage(image, fotoThumbWidth, fotoThumbHeight, scaleTyp),
              vorgang, vorgang.getFotoThumb(), "thumb"));
	}

    /**
     * Speichert das in <code>image</code> übergebene Bild in den Dateinamen,
     * der über <code>prevFilename</code> übergeben wurde. Falls dieser leer
     * ist, wird mittels <code>vorgang</code>-ID und <code>middlePart</code>
     * und einer UUID ein neuer Dateiname generiert und zurück gegeben.
     * @param image Bilddaten
     * @param vorgang Vorgang
     * @param prevFilename Dateiname, darf leer sein
     * @param middlePart Zum Erzeugen eines neuen Dateinamens
     * @return neuer oder übergebener Dateiname
     * @throws IOException
     */
    public String generateFilenameAndWriteFile(byte[] image, Vorgang vorgang,
            String prevFilename, String middlePart) throws IOException {
      String filename;
      if(prevFilename == null) {
        filename = StringUtils.join(new String[] { "ks",
          vorgang.getId().toString(), middlePart, UUID.randomUUID().toString()
        }, "_") + ".jpg";
      }
      else {
        filename = prevFilename;
      }
      Files.write(Paths.get(getPath(), filename), image);
      return filename;
    }

    public BufferedImage imageFromVorgang(Vorgang vorgang) throws IOException {
      InputStream inStream = Files.newInputStream(Paths.get(getPath(), vorgang.getFotoGross()));
      return ImageIO.read(inStream);
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
			BufferedImage image = imageFromVorgang(vorgang);
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
			setImageForVorgang(imageToByteArray(image), vorgang);
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}
    
    public void rotateImageForVorgang(Vorgang vorgang)
	{
		try {
            BufferedImage oldImage = imageFromVorgang(vorgang);
            BufferedImage newImage = new BufferedImage(oldImage.getHeight(), oldImage.getWidth(), oldImage.getType());
            
            Graphics2D graphics2D = (Graphics2D) newImage.getGraphics();
            graphics2D.rotate(Math.toRadians(90), newImage.getWidth() / 2, newImage.getHeight() / 2);
            graphics2D.translate((newImage.getWidth() - oldImage.getWidth()) / 2, (newImage.getHeight() - oldImage.getHeight()) / 2);
            graphics2D.drawImage(oldImage, 0, 0, oldImage.getWidth(), oldImage.getHeight(), null);

            setImageForVorgang(imageToByteArray(newImage), vorgang);
            
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
        JPEGImageWriter writer = (JPEGImageWriter) ImageIO.getImageWritersBySuffix("jpg").next();
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        try {
          writer.setOutput(ImageIO.createImageOutputStream(bos));
          JPEGImageWriteParam param = (JPEGImageWriteParam) writer.getDefaultWriteParam();
          param.setCompressionMode(JPEGImageWriteParam.MODE_EXPLICIT);
          param.setCompressionQuality(0.8f);
          writer.write(image);
          return bos.toByteArray();
        } finally {
          writer.dispose();
          bos.close();
        }
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

    public String getPath() {
        return settingsService.getPropertyValue("image.path");
    }

    public String getUrl() {
        return settingsService.getPropertyValue("image.url");
    }
}
