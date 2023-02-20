package com.driver;

import org.springframework.stereotype.Repository;

import java.util.*;

@Repository
public class OrderRepository
{
    private Map<String, Order> orderMap;
    private Map<String, DeliveryPartner> partnerMap;
    private Map<String, String> orderPartnerMap;
    private Map<String, HashSet<String>> partnerOrderListMap;

    public OrderRepository() {
        this.orderMap = new HashMap<>();
        this.partnerMap = new HashMap<>();
        this.orderPartnerMap = new HashMap<>();
        this.partnerOrderListMap = new HashMap<>();
    }


    public void addOrder(Order order)
    {
        String orderId = order.getId();
        orderMap.put(orderId, order);
    }

    public void addPartner(String partnerId)
    {
        DeliveryPartner partner = new DeliveryPartner(partnerId);
        partnerMap.put(partnerId, partner);
    }

    public void addOrderPartnerPair(String orderId, String partnerId)
    {
        if (orderMap.containsKey(orderId) && partnerMap.containsKey(partnerId))
        {
            if ( !partnerOrderListMap.containsKey(partnerId))
            {
                HashSet<String> orderList = new HashSet<>();
                orderList.add(orderId);
                partnerOrderListMap.put(partnerId,orderList);
                DeliveryPartner partner = partnerMap.get(partnerId);
                partner.setNumberOfOrders(orderList.size());
            }
            else
            {
                HashSet<String> orderList = partnerOrderListMap.get(partnerId);
                orderList.add(orderId);
                partnerOrderListMap.put(partnerId,orderList);
                DeliveryPartner partner = partnerMap.get(partnerId);
                partner.setNumberOfOrders(orderList.size());
            }

            orderPartnerMap.put(orderId, partnerId);
        }
    }

    public Order getOrderById(String orderId)
    {
        if ( !orderMap.containsKey(orderId))
        {
            return null;
        }
        return orderMap.get(orderId);
    }

    public DeliveryPartner getPartnerById(String partnerId)
    {
        if ( !partnerMap.containsKey(partnerId))
        {
            return null;
        }
        return partnerMap.get(partnerId);
    }

    public int getOrderCountByPartnerId(String partnerId)
    {
        if ( !partnerOrderListMap.containsKey(partnerId))
        {
            return 0;
        }
        return partnerOrderListMap.get(partnerId).size();
    }

    public List<String> getOrdersByPartnerId(String partnerId)
    {
        if ( !partnerOrderListMap.containsKey(partnerId))
        {
           return null;
        }

        HashSet<String> orderList = partnerOrderListMap.get(partnerId);
        return new ArrayList<>(orderList);       // converting HashSet to ArrayList
    }

    public List<String> getAllOrders()
    {
        List<String> orderList = new ArrayList<>(orderMap.keySet());
        return orderList;
    }

    public int getCountOfUnassignedOrders()
    {
        return (orderMap.size() - orderPartnerMap.size());
    }

    public int getOrdersLeftAfterGivenTimeByPartnerId(String time, String partnerId)
    {
        int hour = Integer.valueOf(time.substring(0, 2));
        int minutes = Integer.valueOf(time.substring(3));
        int timeInInt = hour*60 + minutes;

        int unDelivered = 0;
        if (partnerOrderListMap.containsKey(partnerId))
        {
            HashSet<String> orderIds = partnerOrderListMap.get(partnerId);
            for (String id: orderIds)
            {
                if (orderMap.containsKey(id))
                {
                    Order currOrder = orderMap.get(id);
                    if (currOrder.getDeliveryTime() > timeInInt)
                    {
                        unDelivered++;
                    }
                }
            }
        }
        return unDelivered;
    }

    public String getLastDeliveryTimeByPartnerId(String partnerId)
    {
        int time = 0;

        if (partnerOrderListMap.containsKey(partnerId))
        {
            HashSet<String> orderIds = partnerOrderListMap.get(partnerId);
            for (String id: orderIds)
            {
                if (orderMap.containsKey(id))
                {
                    Order currOrder = orderMap.get(id);
                    time = Math.max(time, currOrder.getDeliveryTime());
                }
            }
        }

        int hour = time/60;
        int minutes = time%60;

        String hourInString = String.valueOf(hour);
        String minInString = String.valueOf(minutes);
        if(hourInString.length() == 1){
            hourInString = "0" + hourInString;
        }
        if(minInString.length() == 1){
            minInString = "0" + minInString;
        }

        return (hourInString + ":" + minInString);
    }

    public void deletePartnerById(String partnerId)
    {
        // removing from 2nd hashmap
        if (partnerMap.containsKey(partnerId))
        {
            partnerMap.remove(partnerId);
        }

        // searching in 3rd hashmap
        if (partnerOrderListMap.containsKey(partnerId))
        {
            HashSet<String> orderIds = partnerOrderListMap.get(partnerId);
            for (String id: orderIds)
            {
                if (orderPartnerMap.containsKey(id))
                {    // removing from 3rd hashmap
                    orderPartnerMap.remove(id);
                }
            }
            // removing from 4th hashmap
            partnerOrderListMap.remove(partnerId);
        }
    }

    public void deleteOrderById(String orderId)
    {
        // removing from 1st hashmap
        if (orderMap.containsKey(orderId))
        {
            orderMap.remove(orderId);
        }

       // searching in 3rd hashmap
        if (orderPartnerMap.containsKey(orderId))
        {
            // Updating 4th hashmap
            String partnerId = orderPartnerMap.get(orderId);
            HashSet<String> orderIds = partnerOrderListMap.get(partnerId);
            orderIds.remove(orderId);
            partnerOrderListMap.put(partnerId, orderIds);

            // removing from 3rd hashmap
            orderPartnerMap.remove(orderId);

            //change order count of partner
            DeliveryPartner partner = partnerMap.get(partnerId);
            partner.setNumberOfOrders(orderIds.size());
        }
    }
}
