/**
 * (C) Artur Boronat, 2015
 */
package eMarket.controller;

import java.util.List;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;

import eMarket.EMarketApp;
import eMarket.domain.Order;
import eMarket.domain.OrderItem;

@Controller
@RequestMapping("/item")
public class ItemController
{
	public static OrderItem getItem(List<OrderItem> itemList, int id)
	{
		for(OrderItem i : itemList)
		{
			if(i.getId() == id) return i;
		}
		return null;
	}
	
	@RequestMapping(value = "/detail", method = RequestMethod.GET)
    public String orderAdd(@RequestParam(value="orderId", required=true) int orderId,
    @RequestParam(value="itemId", required=false, defaultValue="-1") int itemId,
    Model model)
    {
		ItemFormDto itemFormDto = new ItemFormDto();
    	if(itemId < 0)
    	{
    		itemFormDto.setId(OrderItem.lastId);
    	}
    	else
    	{
    		itemFormDto.setId(orderId);
    		OrderItem thisItem = getItem(OrderController.getOrder(orderId).getItemList(), orderId);
    		
    		itemFormDto.setOrderId(orderId);
    		itemFormDto.setAmount(thisItem.getAmount());
    		itemFormDto.setProductId(thisItem.getProduct().getId());
    	}
    	
    	itemFormDto.setProductList(EMarketApp.getStore().getProductList());
    	
    	model.addAttribute("itemFormDto", itemFormDto);
    	
    	return "form/itemDetail";
    }
}
