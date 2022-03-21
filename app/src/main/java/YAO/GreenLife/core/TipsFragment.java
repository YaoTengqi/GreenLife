package YAO.GreenLife.core;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.greenlife.R;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import YAO.GreenLife.adapter.PostAdapter;
import YAO.GreenLife.bean.post;

public class TipsFragment extends Fragment {
    RecyclerView mRecyclerView;
    List<post> post_list = new ArrayList<>();
    PostAdapter mMyAdapter;
    List<String> post_title = new ArrayList<>(Arrays.asList("节约用水", "少用洗洁精", "节约用电", "交通工具", "节约森林", "选购绿色食品", "选无磷洗衣粉", "用充电电池", "拒绝过度包装", "自带菜篮买菜", "保护野生动物", "利用好可回收物品", "领养一棵树", "避免污染"));
    List<String> post_context = new ArrayList<>(Arrays.asList("随时关上水龙头，别让水白流;看见漏水的龙头一定要拧紧它。尽量使用二次水。例如，淘米或洗菜的水可以浇花;洗脸、洗衣后的水可以留下来擦地、冲厕所。如果您家冲水马桶的容量较大，可以在水箱里放一个装满水的可乐瓶，你的这一小小行动每次可节约1.25升水。",
            "大部分洗涤剂是化学产品，会污染水源。洗餐具时如果油腻过多，可先将残余的油腻倒掉，再用热面汤或热肥皂水等清洗，这样就不会让油污过多地排入水道了。有重油污的厨房用具也可以用苏打加热水来清洗。",
            "随手关灯、少用电器、少用空调为减缓地球温暖化出一把力;不要让电视机长时间处于待机状态，只用遥控关闭，实际并没有完全切断电源。每台彩电待机状态耗电约1.2瓦/小时;使用节能灯，节能灯虽然价格贵，但比普通灯要省电。用温水、热水煮饭，可省电30%.",
            "　出行尽量选择公交车、地铁、自行车，少开私开车，减少尾气排放;有私家车的人尽量使用无铅汽油，因为铅会严重损害人的健康和智力。",
            "少用快餐盒、纸杯、纸盘等，尤其要少用一次快筷子。一次性筷子是日本人发明的。日本的森林覆盖率高达65%，但他们的一次性筷子全靠进口，我国的森林覆盖率不到14%，却是出口一次性筷子的大国",
            "很多蔬菜水果都喷洒过农药、化肥，还有很多食品使用了添加剂。这样的食品会危害健康和智力。所以，要选购不施农药、化肥的新鲜果蔬，少吃含防腐剂的方便快餐食品、有色素的饮料和添加剂的香脆零食。或者认准“绿色食品”标志选购食品也行。",
            "含磷洗衣粉进入水源后，会引起水中藻类疯长，水中含氧量下降，水中生物因缺氧而死亡，",
            "我们日常使用的电池是靠化学作用，通俗地讲就是靠腐蚀作用产生电能的。当其被废弃在自然界时，这些物质便慢慢从电池中溢出，进入土壤或水源，再通过农作物进入人的食物链。用完的干电池攒到30公斤后，可联系当地垃圾回收中心回收。",
            "不少商品如化妆品、保健品的包装费已占到成本的30%～50%.过度包装加重了消费者的经济负担，增加了垃圾量污染了环境。",
            "现在大型超市已经对购物袋进行收费，目的就是为了减少白色污染，买东西时少领取塑料购物袋，上街购物时带上布袋子或菜篮子。在超市买的购物袋也可以重复利用。",
            "拒食野生动物、拒用野生动物制品，不去那些食用野生动物的饭店就餐。不穿珍稀动物毛皮服装，不使用野生动物植物制品，如象牙、虎骨、红木家具等，正所谓没有买卖，就没有伤害。",
            "生活中有许多废物是可以再利用的，如果是完好的物品，可以在自己的城市二手市场卖给需要的人，可回收垃圾可再次生产利用。",
            "参加领养树的活动，在树上挂一个小牌，写上你的名字，定期给它浇水、培土，照料它成长，让它成为你家庭的一员。",
            "如果你去一个地方旅游，不要在你去之前那里是名胜美景，而你走之后那里成为垃圾站。"));


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        View view = View.inflate(getActivity(), R.layout.fragment_tips, null);
        //初始化 环保小贴士 数据
        for (int i = 0; i < post_title.size(); i++) {
            post post = new post();
            post.post_title = post_title.get(i);
            post.post_context = post_context.get(i);
            post_list.add(post);
        }

        /**
         * @Description: RecyclerView的设置
         * @Param: [savedInstanceState]
         * @return: void
         * @Author: YAO
         * @Date: 2022/3/9
         */
        //获取RecyclerView对象
        mRecyclerView = view.findViewById(R.id.hole_rv);


        //设置adapter
        mMyAdapter = new PostAdapter(getActivity(), post_list);
        mRecyclerView.setAdapter(mMyAdapter);

        //设置layoutManager
        LinearLayoutManager layoutManager = new LinearLayoutManager(getActivity());
        mRecyclerView.setLayoutManager(layoutManager);

        //设置Decoration分割线
        DividerItemDecoration decoration = new DividerItemDecoration(getActivity(), DividerItemDecoration.VERTICAL);
        decoration.setDrawable(getResources().getDrawable(R.drawable.divider, null));
        mRecyclerView.addItemDecoration(decoration);


        return view;
    }
}