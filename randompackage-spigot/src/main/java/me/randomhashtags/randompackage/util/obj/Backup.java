package me.randomhashtags.randompackage.util.obj;

import me.randomhashtags.randompackage.universal.UVersion;
import org.apache.commons.io.FileUtils;

import java.io.File;
import java.util.Date;

public final class Backup extends UVersion {
    public Backup() {
        final String folder = DATA_FOLDER.getAbsolutePath() + "_backups";
        final File[] total = new File(folder).listFiles();
        if(total != null && total.length == 10) {
            total[0].delete();
        }
        SCHEDULER.runTaskAsynchronously(RANDOM_PACKAGE, () -> {
            try {
                final String a = toReadableDate(new Date(), "MMMM-dd-yyyy HH_mm_ss z");
                FileUtils.copyDirectory(DATA_FOLDER, new File(folder, a));
                System.out.println("[RandomPackage] Successfully backed up data to folder \"" + a + "\"!");
            } catch (Exception e) {
                e.printStackTrace();
            }
        });
    }
}
