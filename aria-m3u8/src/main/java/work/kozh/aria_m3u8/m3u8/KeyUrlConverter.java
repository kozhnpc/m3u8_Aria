package work.kozh.aria_m3u8.m3u8;

import android.util.Log;

import com.arialyy.aria.core.processor.IKeyUrlConverter;

/**
 * 这种情况是 下载加密key时，连这个地址都是加密的，这时候就需要再转换一下
 */
public class KeyUrlConverter implements IKeyUrlConverter {
    @Override
    public String convert(String m3u8Url, String keyUrl) {
        Log.i("TAG", "key的下载地址被加密：--> 需要解密");
        return null;
    }
}
