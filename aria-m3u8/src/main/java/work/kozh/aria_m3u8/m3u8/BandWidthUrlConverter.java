package work.kozh.aria_m3u8.m3u8;

import android.util.Log;

import com.arialyy.aria.core.processor.IBandWidthUrlConverter;

/**
 * 重定向获取新的m3u8地址
 */
public class BandWidthUrlConverter implements IBandWidthUrlConverter {
    private String url;

    BandWidthUrlConverter(String url) {
        this.url = url;
        Log.i("TAG", "解析的ts地址：--> 原始的m3u8 url :" + url);
    }

    @Override
    public String convert(String bandWidthUrl) {
        int index = url.lastIndexOf("/");
        return url.substring(0, index + 1) + bandWidthUrl;
    }
}
