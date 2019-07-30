package lx.own.view.online.entity;

/**
 * <b>歌词(字)接口</b><br/>
 *
 * @author Lei.X
 * Created on 7/30/2019.
 */
public interface LyricWord {
    int getStartTime();//获取本字的开始时间

    int getEndTime();//获取本字的结束时间

    int getTotalTime();//获取本字总持续时间

    String getWordContent();//获取本字文本
}
