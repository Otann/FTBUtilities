package com.feed_the_beast.ftbu.world.backups;

import com.feed_the_beast.ftbl.lib.BroadcastSender;
import com.feed_the_beast.ftbl.lib.math.MathHelperLM;
import com.feed_the_beast.ftbl.lib.util.LMFileUtils;
import com.feed_the_beast.ftbl.lib.util.LMStringUtils;
import com.feed_the_beast.ftbu.api.FTBULang;
import com.feed_the_beast.ftbu.config.FTBUConfigBackups;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.TextFormatting;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.util.Calendar;
import java.util.List;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

public class ThreadBackup extends Thread
{
    public boolean isDone = false;
    private File src0;

    public ThreadBackup(File w)
    {
        src0 = w;
        setPriority(7);
    }

    public static void doBackup(File src)
    {
        Calendar time = Calendar.getInstance();
        File dstFile = null;
        boolean success = false;
        StringBuilder out = new StringBuilder();
        appendNum(out, time.get(Calendar.YEAR), '-');
        appendNum(out, time.get(Calendar.MONTH) + 1, '-');
        appendNum(out, time.get(Calendar.DAY_OF_MONTH), '-');
        appendNum(out, time.get(Calendar.HOUR_OF_DAY), '-');
        appendNum(out, time.get(Calendar.MINUTE), '-');
        appendNum(out, time.get(Calendar.SECOND), (char) 0);

        try
        {
            List<File> files = LMFileUtils.listAll(src);
            int allFiles = files.size();

            Backups.LOGGER.info("Backing up " + files.size() + " files...");

            if(FTBUConfigBackups.COMPRESSION_LEVEL.getInt() > 0)
            {
                out.append(".zip");
                dstFile = LMFileUtils.newFile(new File(Backups.INSTANCE.backupsFolder, out.toString()));

                long start = System.currentTimeMillis();

                ZipOutputStream zos = new ZipOutputStream(new FileOutputStream(dstFile));
                //zos.setLevel(9);
                zos.setLevel(FTBUConfigBackups.COMPRESSION_LEVEL.getInt());

                long logMillis = System.currentTimeMillis() + 5000L;

                byte[] buffer = new byte[4096];

                Backups.LOGGER.info("Compressing " + allFiles + " files!");

                for(int i = 0; i < allFiles; i++)
                {
                    File file = files.get(i);
                    String filePath = file.getAbsolutePath();
                    ZipEntry ze = new ZipEntry(src.getName() + File.separator + filePath.substring(src.getAbsolutePath().length() + 1, filePath.length()));

                    long millis = System.currentTimeMillis();

                    if(i == 0 || millis > logMillis || i == allFiles - 1)
                    {
                        logMillis = millis + 5000L;
                        Backups.LOGGER.info("[" + i + " | " + MathHelperLM.toSmallDouble((i / (double) allFiles) * 100D) + "%]: " + ze.getName());
                    }

                    zos.putNextEntry(ze);
                    FileInputStream fis = new FileInputStream(file);

                    int len;
                    while((len = fis.read(buffer)) > 0)
                    {
                        zos.write(buffer, 0, len);
                    }
                    zos.closeEntry();
                    fis.close();
                }

                zos.close();

                Backups.LOGGER.info("Done compressing in " + getDoneTime(start) + " seconds (" + LMFileUtils.getSizeS(dstFile) + ")!");
            }
            else
            {
                out.append('/');
                out.append(src.getName());
                dstFile = new File(Backups.INSTANCE.backupsFolder, out.toString());
                dstFile.mkdirs();

                String dstPath = dstFile.getAbsolutePath() + File.separator;
                String srcPath = src.getAbsolutePath();

                long logMillis = System.currentTimeMillis() + 2000L;

                for(int i = 0; i < allFiles; i++)
                {
                    File file = files.get(i);

                    long millis = System.currentTimeMillis();

                    if(i == 0 || millis > logMillis || i == allFiles - 1)
                    {
                        logMillis = millis + 2000L;
                        Backups.LOGGER.info("[" + i + " | " + MathHelperLM.toSmallDouble((i / (double) allFiles) * 100D) + "%]: " + file.getName());
                    }

                    File dst1 = new File(dstPath + (file.getAbsolutePath().replace(srcPath, "")));
                    LMFileUtils.copyFile(file, dst1);
                }
            }

            Backups.LOGGER.info("Created " + dstFile.getAbsolutePath() + " from " + src.getAbsolutePath());
            success = true;

            if(FTBUConfigBackups.DISPLAY_FILE_SIZE.getBoolean())
            {
                String sizeB = LMFileUtils.getSizeS(dstFile);
                String sizeT = LMFileUtils.getSizeS(Backups.INSTANCE.backupsFolder);

                ITextComponent c = FTBULang.BACKUP_END_2.textComponent(getDoneTime(time.getTimeInMillis()), (sizeB.equals(sizeT) ? sizeB : (sizeB + " | " + sizeT)));
                c.getStyle().setColor(TextFormatting.LIGHT_PURPLE);
                BroadcastSender.INSTANCE.addChatMessage(c);
            }
            else
            {
                ITextComponent c = FTBULang.BACKUP_END_1.textComponent(getDoneTime(time.getTimeInMillis()));
                c.getStyle().setColor(TextFormatting.LIGHT_PURPLE);
                BroadcastSender.INSTANCE.addChatMessage(c);
            }
        }
        catch(Exception ex)
        {
            ITextComponent c = FTBULang.BACKUP_FAIL.textComponent(ex.getClass().getName());
            c.getStyle().setColor(TextFormatting.DARK_RED);
            BroadcastSender.INSTANCE.addChatMessage(c);

            ex.printStackTrace();
            if(dstFile != null)
            {
                LMFileUtils.delete(dstFile);
            }
        }

        Backups.INSTANCE.backups.add(new Backup(time.getTimeInMillis(), out.toString().replace('\\', '/'), Backups.INSTANCE.getLastIndex() + 1, success));
        Backups.INSTANCE.cleanupAndSave();
    }

    private static String getDoneTime(long l)
    {
        l = System.currentTimeMillis() - l;

        if(l < 1000L)
        {
            return l + "ms";
        }

        return LMStringUtils.getTimeString(l);
    }

    private static void appendNum(StringBuilder sb, int num, char c)
    {
        if(num < 10)
        {
            sb.append('0');
        }
        sb.append(num);
        if(c != 0)
        {
            sb.append(c);
        }
    }

    @Override
    public void run()
    {
        isDone = false;
        doBackup(src0);
        isDone = true;
    }
}