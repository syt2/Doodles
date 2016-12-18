package party.danyang.doodles.net;

import android.text.TextUtils;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.util.ArrayList;
import java.util.List;

import party.danyang.doodles.entity.Content;
import party.danyang.doodles.entity.SimpleDoodle;

/**
 * Created by dream on 16-8-9.
 */
public class ContentParser {
    public static Content parserDoodleContent(String html) {
//        view-source:https://www.google.com/doodles/qixi-festival-2016
//        view-source:https://www.google.com/doodles/burning-man-festival
        Document doc = Jsoup.parse(html);
        Elements lis = doc.select("li");

        Content doodleContent = new Content();
        for (Element li : lis) {
            if (!li.attr("class").equals("doodle-card")) {
                continue;
            }
            if (li.attr("id").equals("title-card")) {
                doodleContent.setTitle(parserTitle(li));
                doodleContent.setRunDate(parserRunDate(li));
            } else if (li.attr("id").equals("blog-card")) {
                doodleContent.setDoodleDescribe(parserDescribe(li));
            } else if (li.attr("id").equals("history-card")) {
                doodleContent.setHistroyDoodles(parserHistory(li));
            }
        }

        doodleContent.setUrl(getUrl(doc));
        return doodleContent;
    }

    private static String parserRunDate(Element li) {
        Elements divs = li.select("div");
        if (divs == null || divs.size() != 2) {
            return null;
        }
        return divs.get(1).text();
    }

    private static String parserTitle(Element li) {
        Elements h2s = li.select("h2");
        if (h2s == null || h2s.size() != 1) {
            return null;
        }
        return h2s.first().text();
    }

    private static String parserDescribe(Element li) {
        Elements divs = li.select("div");
        if (divs == null || divs.size() != 2) {
            return null;
        }
        String describe = "";
        Elements ps = divs.get(1).select("p");
        for (Element p : ps) {
            describe = TextUtils.concat(describe, "    ", p.text(), "\n").toString();
        }
        return describe;
    }

    private static List<SimpleDoodle> parserHistory(Element li) {
        List<SimpleDoodle> doodles = new ArrayList<>();
        Elements as = li.select("a");
        for (Element a : as) {
            SimpleDoodle doodle = new SimpleDoodle();
            doodle.setTitle(a.attr("title"));
            doodle.setImgUrl(TextUtils.concat("https:", a.select("img").first().attr("src")).toString());
            doodle.setName(a.attr("href").replace("/doodles/", ""));
            doodles.add(doodle);
        }
        return doodles;
    }

    private static String getUrl(Document doc) {
        Elements divs = doc.select("div");
        for (Element div : divs) {
            if (!div.attr("id").equals("doodle-hero")) {
                continue;
            }
            return TextUtils.concat("https:", div.select("img").first().attr("src")).toString();
        }
        return null;
    }

}
