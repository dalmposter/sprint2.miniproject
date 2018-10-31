/**
 * (C) Artur Boronat, 2015
 */
package eMarket.controller;

import java.util.List;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
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
	//adding/editing page for order items
	@RequestMapping(value = "/detail", method = RequestMethod.GET)
    public String orderAdd(@RequestParam(value="orderId", required=true) int orderId,
    @RequestParam(value="itemId", required=false, defaultValue="-1") int itemId,
    Model model)
    {
		ItemFormDto itemFormDto = new ItemFormDto();
		//This is a new item for this order
		//give it IDs and leave it blank
    	if(itemId < 0)
    	{
    		itemFormDto.setId(OrderItem.lastId);
    		itemFormDto.setOrderId(orderId);
    	}
    	//This is an edited item for this order
    	else
    	{
    		//start by setting id
    		itemFormDto.setId(itemId);
    		
    		//find this order and item
    		List<OrderItem> ordersItemList = OrderController.getOrder(orderId).getItemList();
    		OrderItem thisItem = ItemController.getItem(ordersItemList, orderId);
    		
    		//populate dto with existing data to be displayed/edited
    		itemFormDto.setOrderId(orderId);
    		itemFormDto.setAmount(thisItem.getAmount());
    		itemFormDto.setProductId(thisItem.getProduct().getId());
    	}
    	
    	itemFormDto.setProductList(EMarketApp.getStore().getProductList());
    	
    	model.addAttribute("itemFormDto", itemFormDto);
    	
    	return "form/itemDetail";
    }
	
	//Not being able to alter the other classes is quite annoying. Makes a need for these kind of functions
	
	//returns the OrderItem with given id in the given list. Or null if it doesn't exist
	public static OrderItem getItem(List<OrderItem> itemList, int id)
	{
		for(OrderItem i : itemList)
		{
			if(i.getId() == id) return i;
		}
		return null;
	}
	
	//returns the Product with given id in the given list. Or null if it doesn't exist
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
			BindingResult bindingResult,
			@RequestParam String action,
			Model model)
	{
		//chose submit, not cancel
		if(action.equals("Submit"))
		{
			//only care about validating if they chose submit. Valid input doesn't matter if they're cancelling anyway
			if(bindingResult.hasErrors())
			{
				//CHANGEME?
				itemFormDto.setProductList(EMarketApp.getStore().getProductList());
				model.addAttribute("itemFormDto", itemFormDto);
				return "form/itemDetail";
			}
			//if the order item already existed, it's been edited, so just delete it and recreate it
			Order thisOrder = OrderController.getOrder(itemFormDto.getOrderId());
			OrderItem thisItem = ItemController.getItem(thisOrder.getItemList(), itemFormDto.getId());
			if(thisItem != null) thisOrder.getItemList().remove(thisItem);
			
			//this is a new item for this order
			OrderItem newItem = new OrderItem();
			newItem.setAmount(itemFormDto.getAmount());
			newItem.setId(itemFormDto.getId());
			newItem.setProduct(ItemController.getProduct(EMarketApp.getStore().getProductList(), itemFormDto.getProductId()));
			newItem.setCost(newItem.getProduct().getPrice() * newItem.getAmount());
			
			OrderController.getOrder(itemFormDto.getOrderId()).getItemList().add(newItem);
			OrderController.getOrder(itemFormDto.getOrderId()).updateCost();
		}
		/*if(action.equals("Cancel"))
		{
			System.out.println("Cancelled");
		}*/
		
		return "redirect:/order/add?orderId=" + itemFormDto.getOrderId();
	}
}
