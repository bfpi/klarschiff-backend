package de.fraunhofer.igd.klarschiff.web;

import java.text.SimpleDateFormat;
import java.util.Date;

import org.springframework.core.convert.converter.Converter;
import org.springframework.format.FormatterRegistry;
import org.springframework.format.support.FormattingConversionServiceFactoryBean;

import de.fraunhofer.igd.klarschiff.context.AppContext;
import de.fraunhofer.igd.klarschiff.dao.GrenzenDao;
import de.fraunhofer.igd.klarschiff.vo.Kategorie;
import de.fraunhofer.igd.klarschiff.vo.Kommentar;
import de.fraunhofer.igd.klarschiff.vo.StadtteilGrenze;
import de.fraunhofer.igd.klarschiff.vo.Trashmail;
        
/**
 * Klasse zum registieren verschiedener Converter und Formater
 * @author Stefan Audersch (Fraunhofer IGD)
 */
public class ApplicationConversionServiceFactoryBean extends FormattingConversionServiceFactoryBean {

	@Override
	protected void installFormatters(FormatterRegistry registry) {
		super.installFormatters(registry);
	}
	
	Converter<Trashmail, String> getTrashmail2StringConverter() {
        return new Converter<Trashmail, String>() {
            public String convert(Trashmail source) {
                return new StringBuilder().append(source.getPattern()).toString();
            }
        };
    }

	Converter<Kommentar, String> getKommentar2StringConverter() {
        return new Converter<Kommentar, String>() {
            public String convert(Kommentar source) {
                return new StringBuilder().append(source.getText()).append(" ").append(source.getDatum()).toString();
            }
        };
    }

	Converter<Kategorie, String> getKategorie2StringConverter() {
        return new Converter<Kategorie, String>() {
            public String convert(Kategorie source) {
                return new StringBuilder().append(source.getName()).append(" ").append(source.getParent()).toString();
            }
        };
    }

	Converter<String, Date> getString2DateConverter() {
        return new Converter<String, Date>() {
        	SimpleDateFormat formater = new SimpleDateFormat("dd.MM.yyyy");
            public Date convert(String source) {
            	try {
            		return formater.parse(source);
				} catch (Exception e) {
					return null;
				}
            }
        };
    }
	
	Converter<Date, String> getDate2StringConverter() {
        return new Converter<Date, String>() {
        	SimpleDateFormat formater = new SimpleDateFormat("dd.MM.yyyy");
            public String convert(Date source) {
            	try {
            		return formater.format(source);
				} catch (Exception e) {
					return null;
				}
            }
        };
    }
	
	Converter<String, StadtteilGrenze> getString2StadtteilGrenzeConverter() {
        return new Converter<String, StadtteilGrenze>() {
            public StadtteilGrenze convert(String source) {
            	try {
            		return AppContext.getApplicationContext().getBean(GrenzenDao.class).findStadtteilGrenze(Integer.valueOf(source));
				} catch (Exception e) {
					return null;
				}
            }
        };
    }

	public void installLabelConverters(FormatterRegistry registry) {
        registry.addConverter(getTrashmail2StringConverter());
        registry.addConverter(getKommentar2StringConverter());
        registry.addConverter(getKategorie2StringConverter());
        registry.addConverter(getString2DateConverter());
        registry.addConverter(getDate2StringConverter());
        registry.addConverter(getString2StadtteilGrenzeConverter());
    }

	public void afterPropertiesSet() {
        super.afterPropertiesSet();
        installLabelConverters(getObject());
    }
}
