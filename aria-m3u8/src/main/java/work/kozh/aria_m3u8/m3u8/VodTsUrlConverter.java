package work.kozh.aria_m3u8.m3u8;

import android.net.Uri;

import com.arialyy.aria.core.processor.IVodTsUrlConverter;

import java.util.ArrayList;
import java.util.List;

/**
 * 用于解析ts的下载地址
 */
public class VodTsUrlConverter implements IVodTsUrlConverter {
    @Override
    public List<String> convert(String m3u8Url, List<String> tsUrls) {
//        Log.i("TAG", "解析的ts地址：--> 原始的m3u8 url：" + m3u8Url);
        // 转换ts文件的url地址
        Uri uri = Uri.parse(m3u8Url);
        String parentUrl = uri.getScheme() + uri.getHost();
        List<String> newUrls = new ArrayList<>();
        for (String url : tsUrls) {
//            Log.i("TAG", "解析的ts地址：--> " + url);
            if (url.startsWith("/")) {
                //如果以/开头的，则代表是在域名根目录下的， 只需要加入Scheme 和 Host
                newUrls.add(parentUrl + url);
//                Log.i("TAG", "解析的ts地址：--> 以/开头的" + parentUrl + url);
            } else {
                //如果不是以/开头的，那么将原来的m3u8地址的最末尾文件名替换为这个ts地址
                String hostName = m3u8Url.substring(0, m3u8Url.lastIndexOf("/") + 1);
                newUrls.add(hostName + url);
//                Log.i("TAG", "解析的ts地址：--> 不以/开头的" + hostName + url);
            }
        }
        return newUrls; // 返回有效的ts文件url集合
    }
}
