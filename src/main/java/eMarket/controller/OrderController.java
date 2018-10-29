package eMarket.controller;

import java.util.ArrayList;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.WebDataBinder;
import org.springframework.web.bind.annotation.InitBinder;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import java.util.List;

import eMarket.EMarketApp;
import eMarket.domain.Order;

@Controller
@RequestMapping("/order")
public class OrderController
{
    @InitBinder
    protected void initBinder(WebDataBinder binder)
    {
    		binder.addValidators(new DealValidator());
    }
    
    //NEEDS LOGIC
    @RequestMapping(value = "/", method = RequestMethod.GET)
    public String main(Model model)
    {
    	List<Order> temp = new ArrayList<>();
    	temp.add(new Order());
    	temp.get(0).setId();
    	EMarketApp.getStore().setOrderList(temp);
    	model.addAttribute("orderList", EMarketApp.getStore().getOrderList());
		
    	return "form/orderMaster";
    }
    
    @RequestMapping(value = "/add", method = RequestMethod.GET)
    public String orderAdd(Model model)
    {
    	return "form/orderDetail";
    }
}
