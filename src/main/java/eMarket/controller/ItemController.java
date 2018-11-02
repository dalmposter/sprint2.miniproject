/**
 * (C) Artur Boronat, 2015
 */
package eMarket.controller;

import java.util.List;

import javax.swing.JOptionPane;
import javax.validation.Valid;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.validation.ObjectError;
import org.springframework.web.bind.WebDataBinder;
import org.springframework.web.bind.annotation.InitBinder;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;

import eMarket.EMarketApp;
import eMarket.domain.Deal;
import eMarket.domain.Order;
import eMarket.domain.OrderItem;
import eMarket.domain.Product;

@Controller
@RequestMapping("/item")
public class ItemController
{
	@InitBinder
    protected void initBinder(WebDataBinder binder) {
    		binder.addValidators(new ItemValidator());
    }
	
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
	
	//function to return whether a deal is available for an item. If it is, set that items discount accordingly
	//searches for the best deal if multiple exist
	public boolean checkDiscount(OrderItem item, Order order)
	{
		List<Deal> dealList = EMarketApp.getStore().getDealList();
		for(Deal deal : dealList)
		{
			boolean endsAfter = false;
			
			//deal ends after or when this order was created or has no end date
			if(deal.getEndDate() == null) endsAfter = true;
			else if(order.getDate().isBefore(deal.getEndDate()) || order.getDate().isEqual(deal.getEndDate())) endsAfter = true;
			
			if(endsAfter)
			{
				//deal began before or when this order was created
				if(deal.getStartDate().isBefore(order.getDate()) || deal.getStartDate().isEqual(order.getDate()))
				{
					//deal is for the correct product
					if(deal.getProduct().getId() == item.getProduct().getId())
					{
						//set the items discount and return true
						item.setDiscount(deal.getDiscount());
						return true;
					}
				}
			}
		}
		
		//no deal for this order item
		return false;
	}
	
	@RequestMapping(value = "/add", method = RequestMethod.POST)
	public String itemAdd(@Valid @ModelAttribute(value="itemFormDto")ItemFormDto itemFormDto,
			BindingResult bindingResult,
			@RequestParam String action,
			Model model)
	{
		System.out.println("Made it");
		//chose submit, not cancel
		if(action.equals("Submit"))
		{
			//only care about validating if they chose submit. Valid input doesn't matter if they're cancelling anyway
			//either amount is non numerical or none is selected from product list
			if(bindingResult.hasErrors())
			{
				itemFormDto.setProductList(EMarketApp.getStore().getProductList());
				model.addAttribute("itemFormDto", itemFormDto);
				return "form/itemDetail";
			}
			
			//if the order item already existed, it's been edited, so just delete it and recreate it
			Order thisOrder = OrderController.getOrder(itemFormDto.getOrderId());
			OrderItem thisItem = ItemController.getItem(thisOrder.getItemList(), itemFormDto.getId());
			if(thisItem != null) thisOrder.getItemList().remove(thisItem);
			
			//add a new item to the order
			OrderItem newItem = new OrderItem();
			newItem.setAmount(itemFormDto.getAmount());
			newItem.setId(itemFormDto.getId());
			newItem.setProduct(ItemController.getProduct(EMarketApp.getStore().getProductList(), itemFormDto.getProductId()));
			
			if(checkDiscount(newItem, thisOrder))
			{
				newItem.setCost((newItem.getProduct().getPrice() * (1 - newItem.getDiscount())) * newItem.getAmount());
			}
			else
			{
				newItem.setCost(newItem.getProduct().getPrice() * newItem.getAmount());
			}
			
			//was getting a weird problem where the cost was off by tiny fractions. Presumably some internal problem with binary to decimal
			//just round it to 2 decimal places to fix it
			newItem.setCost(Math.round(newItem.getCost() * 100));
			newItem.setCost(newItem.getCost() / 100);
			
			OrderController.getOrder(itemFormDto.getOrderId()).getItemList().add(newItem);
			OrderController.getOrder(itemFormDto.getOrderId()).updateCost();
		}
		/*if(action.equals("Cancel"))
		{
			System.out.println("Cancelled");
		}*/
		
		return "redirect:/order/add?orderId=" + itemFormDto.getOrderId();
	}
	
	//delete item
	@RequestMapping(value = "/delete", method = RequestMethod.GET)
	public String deleteItem(@RequestParam(value="itemId", required=true) int itemId,
			@RequestParam(value="orderId", required=true) int orderId,
			Model model)
	{
		Order thisOrder = OrderController.getOrder(orderId);
		
		if(thisOrder != null)
		{
			thisOrder.getItemList().remove(ItemController.getItem(thisOrder.getItemList(), itemId));
			thisOrder.updateCost();
		}
		
		//return to overview of order
		return "redirect:/order/add?orderId=" + orderId;
	}
}
