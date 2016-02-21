package de.cmklug.liapp;


import java.io.StringWriter;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.List;

public class ShareFormat {

    public String CSV_sidiary (List<GlucoseData> glucosedatas, String unit) {
        SimpleDateFormat dateFormat = new SimpleDateFormat("dd.MM.yyyy;HH:mm") ;
        SimpleDateFormat sqlDateFormat = new SimpleDateFormat("dd.MM.yyyy;HH:mm") ;

        StringWriter file_string = new StringWriter();

        if(unit.equals( "mg/dl")) {
            file_string.write("DAY;TIME;BG_LEVEL");   //00.01.1900;08:00;120;;;;

            for (GlucoseData gd : glucosedatas) {
                file_string.append("\n");
                file_string.append(gd.getDate() + ";" + gd.getGlucoseLevel() + "");
            }

        }else if(unit.equals( "mmol/l")) {
            file_string.write("DAY;TIME;BG_LEVEL_MMOL");   //00.01.1900;08:00;120;;;;

            for (GlucoseData gd : glucosedatas) {
                file_string.append("\n");
                file_string.append(gd.getDate() + ";" + MainActivity.convertmgdlTommoll(gd.getGlucoseLevel()) + "");
            }
        }

        return file_string.toString();
    }

	/* add new Share Formates here */
}
