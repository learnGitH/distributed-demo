package com.haibin.es;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.haibin.es.bean.Article;
import io.searchbox.client.JestClient;
import io.searchbox.client.JestResult;
import io.searchbox.cluster.Health;
import io.searchbox.cluster.NodesInfo;
import io.searchbox.cluster.NodesStats;
import io.searchbox.core.*;
import io.searchbox.indices.*;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

@RunWith(SpringRunner.class)
@SpringBootTest
public class JestApiTest {

    @Autowired
    private JestClient jestClient;

    @Test
    public void resultTest() throws IOException {
        JestResult jestResult = jestClient.execute(new Delete.Builder("1").index("megacorp").type("employee").build());
        if (jestResult.isSucceeded()){
            System.out.println("Success!");
        }
        else {
            System.out.println("Error: " + jestResult.getErrorMessage());
        }
    }

    @Test
    public void indicesTest() throws IOException {
        JestResult result = jestClient.execute(new IndicesExists.Builder("megacorp").build());
        System.out.println(result.isSucceeded());
    }

    @Test
    public void createIndexTest() throws IOException {

        JestResult result = jestClient.execute(new IndicesExists.Builder("employees").build());
        System.out.println(result.isSucceeded());

        jestClient.execute(new CreateIndex.Builder("employees").build());

        JestResult result2 = jestClient.execute(new IndicesExists.Builder("employees").build());
        System.out.println(result2.isSucceeded());

    }

    @Test
    public void createDocumentTest() throws IOException {
        ObjectMapper mapper = new ObjectMapper();
        JsonNode employeeJsonNode = mapper.createObjectNode()
                .put("name", "Michael Pratt")
                .put("title", "Java Developer")
                .put("yearsOfService", 2)
                .set("skills", mapper.createArrayNode()
                        .add("java")
                        .add("spring")
                        .add("elasticsearch"));
        jestClient.execute(new Index.Builder(employeeJsonNode.toString()).index("employees").type("test").build());

    }

    /**
     * 将删除所有的索引
     */
    @Test
    public void deleteIndex() throws IOException {
        DeleteIndex deleteIndex = new DeleteIndex.Builder("article").build();
        JestResult result = jestClient.execute(deleteIndex);
        System.out.println(result.getJsonObject());
    }

    /**
     * 清缓存
     * @throws IOException
     */
    @Test
    public void clearCache() throws IOException {
        ClearCache clearCache = new ClearCache.Builder().build();
        JestResult result = jestClient.execute(clearCache);
        System.out.println(result.getJsonObject());
    }

    /**
     * 关闭索引
     * @throws IOException
     */
    @Test
    public void closeIndex() throws IOException {
        CloseIndex closeIndex = new CloseIndex.Builder("article").build();
        JestResult result = jestClient.execute(closeIndex);
        System.out.println(result.getJsonObject());

    }

    /**
     * 优化索引
     * @throws IOException
     */
    @Test
    public void optimize() throws IOException {
        Optimize optimize = new Optimize.Builder().build();
        JestResult result = jestClient.execute(optimize);
        System.out.println(result.getJsonObject());
    }

    /**
     * 刷新索引
     * @throws Exception
     */
    @Test
    public void flush() throws IOException {
        Flush flush = new Flush.Builder().build();
        JestResult result = jestClient.execute(flush);
        System.out.println(result.getJsonObject());
    }

    /**
     * 判断索引目录是否存在
     * @throws Exception
     */
    @Test
    public void indicesExists() throws IOException {
        IndicesExists indicesExists = new IndicesExists.Builder("article").build();
        JestResult result = jestClient.execute(indicesExists);
        System.out.println(result.getJsonObject());
    }

    /**
     * 查看节点信息
     * @throws Exception
     */
    @Test
    public void nodesInfo() throws IOException {
        NodesInfo nodesInfo = new NodesInfo.Builder().build();
        JestResult result = jestClient.execute(nodesInfo);
        System.out.println(result.getJsonObject());
    }

    /**
     * 查看集群健康信息
     * @throws Exception
     */
    @Test
    public void health() throws IOException {
        Health health = new Health.Builder().build();
        JestResult result = jestClient.execute(health);
        System.out.println(result.getJsonObject());
    }

    /**
     * 节点状态
     * @throws Exception
     */
    @Test
    public void nodesStas() throws IOException {
        NodesStats nodesStats = new NodesStats.Builder().build();
        JestResult result = jestClient.execute(nodesStats);
        System.out.println(result.getJsonObject());
    }

    /**
     * 更新Document
     * @throws Exception
     */
    @Test
    public void updateDocument() throws IOException {
        Article article = new Article();
        article.setId(1);
        article.setTitle("中国3颗卫星拍到阅兵现场高清照");
        article.setContent("据中国资源卫星应用中心报道，9月3日，纪念中国人民抗日战争暨世界反法西斯战争胜利70周年大阅兵在天安门广场举行。资源卫星中心针对此次盛事，综合调度在轨卫星，9月1日至3日连续三天持续观测首都北京天安门附近区域，共计安排5次高分辨率卫星成像。在阅兵当日，高分二号卫星、资源三号卫星及实践九号卫星实现三星联合、密集观测，捕捉到了阅兵现场精彩瞬间。为了保证卫星准确拍摄天安门及周边区域，提高数据处理效率，及时制作合格的光学产品，资源卫星中心运行服务人员从卫星观测计划制定、复核、优化到系统运行保障、光学产品图像制作，提前进行了周密部署，并拟定了应急预案，为圆满完成既定任务奠定了基础。");
        article.setPubdate(new Date());
        article.setAuthor("匿名");
        article.setSource("新华网");
        article.setUrl("http://news.163.com/15/0909/07/B32AGCDT00014JB5.html");
        String script = "{" +
                "    \"doc\" : {" +
                "        \"title\" : \""+article.getTitle()+"\"," +
                "        \"content\" : \""+article.getContent()+"\"," +
                "        \"author\" : \""+article.getAuthor()+"\"," +
                "        \"source\" : \""+article.getSource()+"\"," +
                "        \"url\" : \""+article.getUrl()+"\"," +
                "        \"pubdate\" : \""+new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss").format(article.getPubdate())+"\"" +
                "    }" +
                "}";
        Update update = new Update.Builder(script).index("haibin").type("article").id("1").build();
        JestResult result = jestClient.execute(update);
        System.out.println(result.getJsonString());
    }

    /**
     * 删除Document
     * @throws Exception
     */
    @Test
    public void deleteDocument() throws IOException {
        Delete delete = new Delete.Builder("1").index("haibin").type("article").build();
        JestResult result = jestClient.execute(delete);
        System.out.println(result.getJsonString());
    }

    /**
     * 获取Document
     * @throws Exception
     */
    @Test
    public void getDocument() throws IOException {
        Get get = new Get.Builder("haibin","1").type("article").build();
        JestResult result = jestClient.execute(get);
        Article article = result.getSourceAsObject(Article.class);
        System.out.println(article.getTitle()+","+article.getContent());
    }

    /**
     * 查询全部
     * @throws Exception
     */
    /*@Test
    public void searchAll() throws IOException {
        SearchSourceBuilder searchSourceBuilder = new SearchSourceBuilder();
        searchSourceBuilder.query(QueryBuilders.matchAllQuery());
        Search search = new Search.Builder(searchSourceBuilder.toString()).addIndex("haibin").build();
        SearchResult result = jestClient.execute(search);
        System.out.println("本次查询共查到："+result.getTotal()+"篇文章！");
        List<SearchResult.Hit<Article,Void>> hits = result.getHits(Article.class);
        for (SearchResult.Hit<Article, Void> hit : hits) {
            Article source = hit.source;
            System.out.println("标题："+source.getTitle());
            System.out.println("内容："+source.getContent());
            System.out.println("url："+source.getUrl());
            System.out.println("来源："+source.getSource());
            System.out.println("作者："+source.getAuthor());
        }
    }*/

    @Test
    public void bulkIndex() throws Exception {
        Article article1 = new Article(1,"中国获租巴基斯坦瓜达尔港2000亩土地 为期43年","巴基斯坦(瓜达尔港)港务局表示，将把瓜达尔港2000亩土地，长期租赁给中方，用于建设(瓜达尔港)首个经济特区。分析指，瓜港首个经济特区的建立，不但能对巴基斯坦的经济发展模式，产生示范作用，还能进一步提振经济水平。" +
                "据了解，瓜达尔港务局于今年6月完成了1500亩土地的征收工作，另外500亩的征收工作也将很快完成，所征土地主要来自巴基斯坦海军和俾路支省政府紧邻规划的集装箱货堆区，该经济特区相关基础设施建设预计耗资3500万美元。瓜港务局表示，目前已将这2000亩地租赁给中国，中方将享有43年的租赁权。巴基斯坦前驻华大使马苏德·汗表示，这对提振巴基斯坦经济大有助益：“我认为巴基斯坦所能获得的最大益处主要还是在于经济互通领域”。" +
                "为了鼓励国内外投资者到经济特区投资，巴政府特别针对能源、税制等国内投资短板，专门为投资者出台了利好政策来鼓励投资。这些举措包括三个方面，一是保障能源，即保障经济特区的电力和天然气供应方面享有优先权；二是减免税收，即为经济特区投资的企业提供为期10年的税收假期，并为企业有关生产设备的进口给予免关税待遇；三是一站式服务，即为有意投资经济特区的投资者提供一站式快捷服务，包括向投资者提供有关优惠政策的详尽资讯。马苏德·汗还指出，为了让巴基斯坦从投资中受益，巴政府尽可能提供了各种优惠政策：“(由于)巴基斯坦想从中获益，为此做了大量力所能及的工作”。" +
                "巴政府高度重视瓜港经济特区建设，将其视为现代化港口的“标配”，期待其能够最大化利用瓜港深水良港的自然禀赋，吸引国内外投资者建立生产、组装以及加工企业。为鼓励投资，巴方还开出了20年免税的优惠条件。" +
                "除了瓜达尔港，中巴还有哪些项目？" +
                "根据中国和巴基斯坦两国4月20日发表的联合声明，双方将积极推进喀喇昆仑公路升级改造二期(塔科特至哈维连段)、瓜达尔港东湾快速路、新国际机场、卡拉奇至拉合尔高速公路(木尔坦至苏库尔段)、拉合尔轨道交通橙线、海尔－鲁巴经济区、中巴跨境光缆、在巴实行地面数字电视传输标准等重点合作项目及一批基础设施和能源电力项目。" +
                "应巴基斯坦总统侯赛因和总理谢里夫邀请，中国国家主席习近平于4月20日至21日对巴基斯坦进行国事访问。访问期间，习近平主席会见了侯赛因总统、谢里夫总理以及巴基斯坦议会、军队和政党领导人，同巴各界人士进行了广泛接触。" +
                "双方高度评价将中巴经济走廊打造成丝绸之路经济带和21世纪海上丝绸之路倡议重大项目所取得的进展。巴方欢迎中方设立丝路基金并将该基金用于中巴经济走廊相关项目。" +
                "巴方将坚定支持并积极参与“一带一路”建设。丝路基金宣布入股三峡南亚公司，与长江三峡集团等机构联合开发巴基斯坦卡洛特水电站等清洁能源项目，这是丝路基金成立后的首个投资项目。丝路基金愿积极扩展中巴经济走廊框架下的其他项目投融资机会，为“一带一路”建设发挥助推作用。" +
                "双方认为，“一带一路”倡议是区域合作和南南合作的新模式，将为实现亚洲整体振兴和各国共同繁荣带来新机遇。" +
                "双方对中巴经济走廊建设取得的进展表示满意，强调走廊规划发展将覆盖巴全国各地区，造福巴全体人民，促进中巴两国及本地区各国共同发展繁荣。" +
                "双方同意，以中巴经济走廊为引领，以瓜达尔港、能源、交通基础设施和产业合作为重点，形成“1+4”经济合作布局。双方欢迎中巴经济走廊联委会第四次会议成功举行，同意尽快完成《中巴经济走廊远景规划》。", "http://news.163.com/15/0909/14/B332O90E0001124J.html", Calendar.getInstance().getTime(), "中国青年网", "匿名");
        Article article2 = new Article(2,"中央党校举行秋季学期开学典礼 刘云山出席讲话","新华网北京9月7日电 中共中央党校7日上午举行2015年秋季学期开学典礼。中共中央政治局常委、中央党校校长刘云山出席并讲话，就深入学习贯彻习近平总书记系列重要讲话精神、坚持党校姓党提出要求。" +
                "刘云山指出，党校姓党是党校工作的根本原则。坚持党校姓党，重要的是坚持坚定正确的政治方向、贯彻实事求是的思想路线、落实从严治校的基本方针。要把党的基本理论教育和党性党风教育作为主课，深化中国特色社会主义理论体系学习教育，深化对习近平总书记系列重要讲话精神的学习教育，深化党章和党纪党规的学习教育。要坚持实事求是的思想方法和工作方法，弘扬理论联系实际的学风，提高教学和科研工作的针对性实效性。要严明制度、严肃纪律，把从严治校要求体现到党校工作和学员管理各方面，使党校成为不正之风的“净化器”。" +
                "刘云山指出，坚持党校姓党，既是对党校教职工的要求，也是对党校学员的要求。每一位学员都要强化党的意识，保持对党忠诚的政治品格，忠诚于党的信仰，坚定道路自信、理论自信、制度自信；忠诚于党的宗旨，牢记为了人民是天职、服务人民是本职，自觉践行党的群众路线；忠诚于党的事业，勤政敬业、先之劳之、敢于担当，保持干事创业的进取心和精气神。要强化党的纪律规矩意识，经常看一看党的政治准则、组织原则执行得怎么样，看一看党的路线方针政策落实得怎么样，看一看重大事项请示报告制度贯彻得怎么样，找差距、明不足，做政治上的明白人、遵规守纪的老实人。" +
                "刘云山强调，领导干部来党校学习，就要自觉接受党的优良作风的洗礼，修好作风建设这门大课。要重温党的光荣传统，学习革命先辈、英雄模范和优秀典型的先进事迹和崇高风范，自觉践行社会主义核心价值观，永葆共产党人的先进性纯洁性，以人格力量传递作风建设正能量。要认真落实党中央关于从严治党、改进作风的一系列要求，贯彻从严精神、突出问题导向，把自己摆进去、把职责摆进去，推动思想问题和实际问题一起解决，履行好党和人民赋予的职责使命。" +
                "赵乐际出席开学典礼。" +
                "中央有关部门负责同志，中央党校校委成员、全体学员和教职工在主会场参加开学典礼。中国浦东、井冈山、延安干部学院全体学员和教职工在分会场参加开学典礼。", "http://news.163.com/15/0907/20/B2UGF9860001124J.html", Calendar.getInstance().getTime(), "新华网", "NN053");
        Article article3 = new Article(3,"俞正声率中央代表团赴大昭寺看望宗教界人士","国际在线报道：7号上午，赴西藏出席自治区50周年庆祝活动的中共中央政治局常委、全国政协主席俞正声与中央代表团主要成员赴大昭寺慰问宗教界爱国人士。俞正声向大昭寺赠送了习近平总书记题写的“加强民族团结，建设美丽西藏”贺幛，珐琅彩平安瓶，并向僧人发放布施，他在会见僧众时表示，希望藏传佛教坚持爱国爱教传统。" +
                "至今已有1300多年历史的大昭寺，在藏传佛教中拥有至高无上的地位。西藏的寺院归属于某一藏传佛教教派，大昭寺则是各教派共尊的神圣寺院。一年四季不论雨雪风霜，大昭寺外都有从四面八方赶来磕长头拜谒的虔诚信众。" +
                "7号上午，赴西藏出席自治区50周年庆祝活动的中共中央政治局常委、全国政协主席俞正声与中央代表团主要成员专门到大昭寺看望僧众，“我代表党中央、国务院和习主席向大家问好。藏传佛教有许多爱国爱教的传统，有许多高僧大德维护祖国统一和民族团结，坚持传播爱国爱教的正信。党和政府对此一贯给予充分肯定，并对藏传佛教的发展给予了支持。”" +
                "俞正声回忆这已是他自1995年以来第三次来大昭寺。他表示，过去二十年发生了巨大的变化，而藏传佛教的发展与祖国的发展、西藏的发展息息相关。藏传佛教要更好发展必须与社会主义社会相适应。他也向僧人们提出期望：“佛教既是信仰，也是一种文化和学问，希望大家不断提高自己对佛教的认识和理解，提高自己的水平。希望大家更好地管理好大昭寺，搞好民主管理、科学管理，使我们的管理更加规范。”" +
                "今天是中央代表团抵达拉萨的第二天，当天安排有多项与自治区成立五十周年相关活动。继上午接见自治区领导成员、离退休老同志、各族各界群众代表宗教界爱国人士，参观自治区50年成就展外，中央代表团下午还慰问了解放军驻拉萨部队、武警总队等。当天晚些时候，庆祝西藏自治区成立50周年招待会、文艺晚会将在拉萨举行。（来源：环球资讯）", "http://news.163.com/15/0907/16/B2U3O30R00014JB5.html", Calendar.getInstance().getTime(), "国际在线", "全宇虹");
        Article article4 = new Article(4,"张德江:发挥人大主导作用 加快完备法律规范体系","新华网广州9月7日电 中共中央政治局常委、全国人大常委会委员长张德江9月6日至7日在广东出席第21次全国地方立法研讨会，并在佛山市就地方人大工作进行调研。他强调，要全面贯彻落实党的十八大和十八届三中、四中全会精神，深入学习贯彻习近平总书记系列重要讲话精神，认真实施立法法，充分发挥人大及其常委会在立法工作中的主导作用，抓住提高立法质量这个关键，加快形成完备的法律规范体系，为协调推进“四个全面”战略布局提供法治保障。" +
                "张德江指出，立法权是人大及其常委会的重要职权。做好新形势下立法工作，要坚持党的领导，贯彻党的路线方针政策和中央重大决策部署，切实增强思想自觉和行动自觉。要牢固树立依法立法、为民立法、科学立法理念，尊重改革发展客观规律和法治建设内在规律，加强重点领域立法，做到立法主动适应改革和经济社会发展需要。充分发挥在立法工作中的主导作用，把握立项、起草、审议等关键环节，科学确定立法项目，协调做好立法工作，研究解决立法中的重点难点问题，建立健全立法工作格局，形成立法工作合力。" +
                "张德江说，地方性法规是我国法律体系的重要组成部分。地方立法关键是在本地特色上下功夫、在有效管用上做文章。当前要落实好立法法的规定，扎实推进赋予设区的市地方立法权工作，明确步骤和时间，做好各项准备工作，标准不能降低，底线不能突破，坚持“成熟一个、确定一个”，确保立法质量。" +
                "张德江强调，加强和改进立法工作，要有高素质的立法工作队伍作为保障。要把思想政治建设摆在首位，全面提升专业素质能力，充实力量，培养人才，努力造就一支忠于党、忠于国家、忠于人民、忠于法律的立法工作队伍。" +
                "张德江一直非常关注地方人大特别是基层人大工作。在粤期间，他来到佛山市人大常委会，详细询问立法、监督等工作情况，希望他们与时俱进、开拓创新，切实担负起宪法法律赋予的职责。他走进南海区人大常委会、顺德区乐从镇人大主席团，同基层人大代表和人大工作者亲切交谈，肯定基层人大代表联系群众的有益做法，强调人大代表不能脱离人民群众，必须把人民利益放在心中，时刻为群众着想，听取群众意见，反映群众意愿，帮助群众解决实际问题。张德江指出，县乡人大在基层治理体系和治理能力建设中具有重要作用。要贯彻落实中央关于加强县乡人大工作和建设的精神，认真实施新修改的地方组织法、选举法、代表法，不断提高基层人大工作水平，推动人大工作迈上新台阶。" +
                "中共中央政治局委员、广东省委书记胡春华参加上述活动。", "http://news.163.com/15/0907/20/B2UGEUTJ00014PRF.html", Calendar.getInstance().getTime(), "新华网", "陈菲");

        Bulk bulk = new Bulk.Builder()
                .defaultIndex("haibin")
                .defaultType("article")
                .addAction(Arrays.asList(
                        new Index.Builder(article1).build(),
                        new Index.Builder(article2).build(),
                        new Index.Builder(article3).build(),
                        new Index.Builder(article4).build()
                )).build();
        jestClient.execute(bulk);
    }

}
