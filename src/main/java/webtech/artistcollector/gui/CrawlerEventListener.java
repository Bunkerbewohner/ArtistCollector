package webtech.artistcollector.gui;

import webtech.artistcollector.interfaces.PageInfo;

import java.util.EventListener;

public interface CrawlerEventListener extends EventListener{

    void crawlerFinished(PageInfo results);

}
