/**
 * (C) Artur Boronat, 2015
 */
package eMarket.controller;

import java.util.List;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;

import eMarket.EMarketApp;
import eMarket.domain.Order;
import eMarket.domain.OrderItem;
import eMarket.domain.Product;

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
    		itemFormDto.setOrderId(orderId);
    	}
    	else
    	{
    		itemFormDto.setId(itemId);
    		OrderItem thisItem = getItem(OrderController.getOrder(orderId).getItemList(), orderId);
    		
    		itemFormDto.setOrderId(orderId);
    		itemFormDto.setAmount(thisItem.getAmount());
    		itemFormDto.setProductId(thisItem.getProduct().getId());
    		itemFormDto.setProductList(EMarketApp.getStore().getProductList());
    		System.out.println(itemFormDto.getOrderId());
    	}
    	
    	itemFormDto.setProductList(EMarketApp.getStore().getProductList());
    	
    	model.addAttribute("itemFormDto", itemFormDto);
    	
    	return "form/itemDetail";
    }
	
	public static boolean hasId(List<OrderItem> items, int id)
	{
		for(OrderItem o : items)
		{
			if(o.getId() == id) return true;
		}
		
		return false;
	}
	
	public static Product getProduct(List<Product> products, int id)
	{
		for(Product p : products)
		{
			if(p.getId() == id) return p;
		}
		
		return null;
	}
	
	@RequestMapping(value = "/add", method = RequestMethod.POST)
	public String itemAdd(@ModelAttribute(value="itemFormDto")ItemFormDto itemFormDto,
			@RequestParam String action,
			Model model)
	{
		if(action.equals("Submit"))
		{
			//if the order item already existed, it's been edited, so just delete it and recreate it
			System.out.println("thisOrderId: " + itemFormDto.getOrderId());
			Order thisOrder = OrderController.getOrder(itemFormDto.getOrderId());
			System.out.println("thisOrder: " + thisOrder);
			OrderItem thisItem = ItemController.getItem(thisOrder.getItemList(), itemFormDto.getId());
			thisOrder.getItemList().remove(thisItem);
			
			//this is a new item for this order
			OrderItem newItem = new OrderItem();
			newItem.setAmount(itemFormDto.getAmount());
			newItem.setId(itemFormDto.getId());
			newItem.setProduct(ItemController.getProduct(EMarketApp.getStore().getProductList(), itemFormDto.getProductId()));
			newItem.setCost(newItem.getProduct().getPrice() * newItem.getAmount());
			
			OrderController.getOrder(itemFormDto.getOrderId()).getItemList().add(newItem);
			OrderController.getOrder(itemFormDto.getOrderId()).updateCost();

		}
		if(action.equals("Cancel"))
		{
			System.out.println("Cancelled");
		}
		
		return "redirect:/order/add?orderId=" + itemFormDto.getOrderId();
	}
}
