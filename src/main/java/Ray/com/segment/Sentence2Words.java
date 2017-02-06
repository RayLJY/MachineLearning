package Ray.com.segment;

import org.lionsoul.jcseg.tokenizer.ASegment;
import org.lionsoul.jcseg.tokenizer.core.*;

import java.io.IOException;
import java.io.Serializable;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by ray on 17/2/6.
 *
 * This class that split sentences to words is a basic class
 *
 */
public class Sentence2Words implements Serializable {

    private ASegment seg=null;

    public Sentence2Words(){
//        JcsegTaskConfig config = new JcsegTaskConfig(Sentence2Words.class.getResource("").getPath()+"/jcseg.properties");
        JcsegTaskConfig config = new JcsegTaskConfig();
        ADictionary dic = DictionaryFactory.createDefaultDictionary(config);
        try {
            seg = (ASegment) SegmentFactory.createJcseg(JcsegTaskConfig.COMPLEX_MODE, config, dic);
        } catch (JcsegException e) {
            e.printStackTrace();
        }
    }
    /**
     * 分词
     *
     * @param sentence
     * @return
     */
    public List<String> splitSentence2WordList(String sentence) {
        ArrayList<String> list = new ArrayList<>();
        try {
            seg.reset(new StringReader(sentence));
            IWord word;
            while ((word = seg.next()) != null) {
                list.add(word.getValue());
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return list;
    }

    /**
     * 去除指定的字符支持正则，再分词
     *
     * @param sentence
     * @return
     */
    public List<String> splitSentence2WordList(String sentence,String[] regs) {
        ArrayList<String> list = new ArrayList<>();
        sentence = clean(sentence,regs);
        try {
            seg.reset(new StringReader(sentence));
            IWord word;
            while ((word = seg.next()) != null) {
                list.add(word.getValue());
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return list;
    }

    private String clean(String sentence,String[] regs){
        for (String reg : regs ) {
          sentence = sentence.replaceAll(reg , " ");
        }
        return sentence;
    }

    public static void main(String[] args) {
        String str = "土生土长的深圳卫视编导阿豆，" +
                "带你行走找寻深圳静谧的" +
                "另一面<p>，带你看深圳本地人眼中的「大」城深圳。</p><p>海景" +
                "漫步or城市风光，随心选择你享受的惬意深圳。</p><p>你在看风景，我在看" +
                "你，边走边拍风景中的你。</p>";
        Sentence2Words s = new Sentence2Words();
        List<String> sl = s.splitSentence2WordList(str, new String[]{"<([^>]*)>","，","「","」"});
        System.out.println(sl);
    }
}