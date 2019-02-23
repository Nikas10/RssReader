package com.company.nikas;

import com.company.nikas.config.AppConfiguration;
import com.company.nikas.integrations.impl.ApacheRssReader;
import com.company.nikas.model.RssConfiguration;
import com.company.nikas.model.consts.RssType;
import com.company.nikas.system.ActiveStreamMonitor;
import org.apache.log4j.Level;
import org.apache.log4j.Logger;
import org.apache.log4j.PropertyConfigurator;

import java.util.Timer;
import java.util.TimerTask;

public class App
{
    static Logger logger = Logger.getLogger(App.class);

    public static void main( String[] args )
    {
        logger.setLevel(Level.INFO);

        PropertyConfigurator.configure(App.class.getResourceAsStream("/log4j.properties"));

        AppConfiguration appConfiguration = new AppConfiguration();
        Timer timer = new Timer();
        String name = "CNN Top stories";
        RssConfiguration rssConfiguration = new RssConfiguration();
        rssConfiguration.setFilePath("G:\\DISKRELATED\\UNIVER\\git\\RssReader\\target\\x.txt");
        rssConfiguration.setRequestSchedule(1000);
        rssConfiguration.setRssType(RssType.RSS);
        rssConfiguration.setUrl("http://rss.cnn.com/rss/cnn_topstories.rss");
        appConfiguration.getRssFeeds().put(name, rssConfiguration);
        TimerTask rssTest = new ApacheRssReader(appConfiguration, name);
        System.out.println("scheduling the task");
        timer.schedule(rssTest, rssConfiguration.getRequestSchedule());
        Thread thread = new Thread(new ActiveStreamMonitor(appConfiguration));
        thread.start();
        try {
            Thread.sleep(5000);
            logger.info("sleep finished");
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        thread.interrupt();
        rssTest.cancel();
        timer.cancel();
    }
}
