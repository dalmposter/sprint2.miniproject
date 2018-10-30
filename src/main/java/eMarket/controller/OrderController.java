package eMarket.controller;

import java.util.ArrayList;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.List;

import eMarket.EMarketApp;
import eMarket.domain.Order;

@Controller
@RequestMapping("/order")
public class OrderController
{
    
    //NEEDS LOGIC
    @RequestMapping(value = "/", method = RequestMethod.GET)
    public String main(Model model)
    {
    	model.addAttribute("orderList", EMarketApp.getStore().getOrderList());
		
    	return "form/orderMaster";
    }
    
    public static Order getOrder(int id)
    {
    	for(Order o : EMarketApp.getStore().getOrderList())
		{
			if(o.getId() == id)
			{
				return o;
			}
		}
    	return null;
    }
    
    @RequestMapping(value = "/add", method = RequestMethod.GET)
    public String orderAdd(@RequestParam(value="orderId", required=false, defaultValue="-1") int orderId,
    Model model)
    {
    	Order order = null;
    	if(orderId < 0)
    	{
    		order = new Order();
    		order.setId();
    		orderId = order.getId();
    		EMarketApp.getStore().getOrderList().add(order);
    	}
    	else
    	{
    		order = getOrder(orderId);
    	}
    	
    	model.addAttribute("order", order);
    	
    	return "form/orderDetail";
    }
    
    @RequestMapping(value = "/delete", method = RequestMethod.GET)
    public String orderDelete(@RequestParam(value="orderId", required=false, defaultValue="-1") int orderId,
    Model model)
    {
    	EMarketApp.getStore().getOrderList().remove(getOrder(orderId));
    	
    	return "form/orderMaster";
    }
}
