package com.point.cart;

import com.alibaba.fastjson.JSON;
import com.point.cart.common.rsp.MessageRsp;
import com.point.cart.controller.CartPointController;
import com.point.cart.message.req.CartScreenInfoReq;
import com.point.cart.message.req.ScreenInfoReq;
import com.point.cart.message.req.SearchScreenMediaInfoReq;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

import javax.annotation.Resource;
import java.util.ArrayList;

@RunWith(SpringRunner.class)
@SpringBootTest
public class PointApplicationTests {
	private Logger logger = LoggerFactory.getLogger(PointApplicationTests.class);
	@Resource
	private CartPointController controller;

	/**
	 *  查询屏数据
	 */
	@Test
	public void searchScreenMediaInfo() {
		SearchScreenMediaInfoReq req = new SearchScreenMediaInfoReq();
		req.setSelectMode(0);
		req.setTimeBucket("A");
		req.setNumber(1);
		req.setTimeLength(30);
		req.setDevType("立式");
		req.setScreenLocation("下屏");
		req.setStartTime(1510675200L);
		req.setEndTime(1510761599L);
		MessageRsp rsp = controller.searchScreenMediaInfo(req);
		logger.info(JSON.toJSONString(rsp));
	}

	/**
	 *  添加屏数据
	 */
	@Test
	public void addPointCart(){
		SearchScreenMediaInfoReq req = new SearchScreenMediaInfoReq();
		req.setSelectMode(0);
		req.setTimeBucket("A");
		req.setNumber(1);
		req.setTimeLength(30);
		req.setDevType("立式");
		req.setScreenLocation("下屏");
		req.setStartTime(1510675200L);
		req.setEndTime(1510761599L);
		req.setScreenArr(new ArrayList<ScreenInfoReq>(){
			{
				add(new ScreenInfoReq("310100","上海市","dftc-003950-73"));
				add(new ScreenInfoReq("440100","广州市","dftc-005401-73"));
				add(new ScreenInfoReq("440100","广州市","dftc-005388-73"));
				add(new ScreenInfoReq("410100","郑州市","dftc-015245-73"));
			}
		});
		MessageRsp rsp = controller.addPointCart(req);
		logger.info(JSON.toJSONString(rsp));
	}

	/**
	 * 根据购物车编号按照市进行分组展示
	 */
	@Test
	public void getCartInfo(){
		MessageRsp rsp = controller.getCartInfo("2447531caf784149bf05076dd947a900",1.2F,1,1);
		logger.info(JSON.toJSONString(rsp));
	}

	/**
	 * 根据购物车编号+城市名称或者选中屏资产编号进行删除
	 */
	@Test
	public void deleteCartScreen(){
		CartScreenInfoReq req = new CartScreenInfoReq();
		req.setCartNumber("2447531caf784149bf05076dd947a900");
		req.setScreenList(new ArrayList<String>(){
			{
				add("dftc-038995-74");
			}
		});

		MessageRsp rsp = controller.deleteBySelectScreen(req);
		logger.info(JSON.toJSONString(rsp));
	}

	/**
	 *  确认订单 将其发布到正式订单中
	 */
	@Test
	public void confirmOrder(){
//		MessageRsp rsp = controller.confirmOrder(null, null);
//		logger.info(JSON.toJSONString(rsp));
	}
}
