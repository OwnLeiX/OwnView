package lx.own.view.online.entity;

import java.util.List;

/**
 * <b>歌词(行)接口</b><br/>
 *
 * @author Lei.X
 * Created on 7/30/2019.
 */
public interface LyricRows {
    String getContent();//获取本行歌词

    int getStartTime();//获取本行开始时间

    List<LyricWord> getLrcWords();//获取本行的每个字信息
}
