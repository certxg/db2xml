
===========
mysql sales

select 
invoice.invoice_id as "invoice.invoice_id", 
invoice.customer_id as "invoice.customer_id", 
invoice.total as "invoice.total", 
item.invoice_id as "item.invoice_id", 
item.item_id as "item.item_id", 
item.product_id as "item.product_id", 
item.quantity as "item.quantity", 
item.cost as "item.cost", 
product.product_id as "product.product_id", 
product.name as "product.name", 
product.price as "product.price", 
customer.customer_id as "customer.customer_id", 
customer.first_name as "customer.first_name", 
customer.last_name as "customer.last_name", 
customer.street as "customer.street", 
customer.city as "customer.city" 
from invoice, item, product, customer 
where invoice.invoice_id=item.invoice_id 
and item.product_id=product.product_id 
and invoice.customer_id=customer.customer_id 
and invoice.invoice_id = 10


=================
json output: 

How to store it in the hbase:

{"sales":
    {"invoice":
        {   "total":"3274.5",
            "invoiceId":"10",
            "customerId":"24",
            "customer":
            	{	"customerId":"24",
            		"lastName":"Sommer",
            		"street":"333 Upland Pl.",
            		"firstName":"James",
            		"city":"Olten"
            	}
            
            "item":[
                    {"product":
                        {	"productId":"20",
                        	"price":"20",
                        	"name":"Ice Tea Telephone"
                        },
                        "invoiceId":"10",
                        "itemId":"0",
                        "quantity":"1",
                        "cost":"30",
                        "productId":"20"
                    },
                    {"product":{"productIid":"22","price":"6.6","name":"Iron Iron"},"invoiceId":"10","item":"8","quantity":"11","cost":"9.9","productId":"22"}
                ],
        }
    }
}

<<json -> sql>>


json -> hbase convertion: 1) key -> element; cq&v 2) key -> set; cf= cf_cf->next 3) key -> array
============

create 'sales', 'invoice', 'invoice_customer','invoice_item','invoice_item_product'

put 'sales','invoiceid_10','invoice:total','3274.5'
put 'sales','invoiceid_10','invoice:customerId','24'
put 'sales','invoiceid_10','invoice:invoiceId','10'

put 'sales','invoiceid_10','invoice_customer:customerId','24'
put 'sales','invoiceid_10','invoice_customer:lastname','Sommer'
put 'sales','invoiceid_10','invoice_customer:firstname','James'
put 'sales','invoiceid_10','invoice_customer:city','Olten'
put 'sales','invoiceid_10','invoice_customer:street','333 Upland Pl.'

put 'sales','invoiceid_10','invoice_item_0:itemid','0'
put 'sales','invoiceid_10','invoice_item_0:invoiceid','10'
put 'sales','invoiceid_10','invoice_item_0:quantity','1'
put 'sales','invoiceid_10','invoice_item_0:cost','30'
put 'sales','invoiceid_10','invoice_item_0:productid','20'

put 'sales','invoiceid_10','invoice_item_product_0:productid','20'
put 'sales','invoiceid_10','invoice_item_product_0:price','20'
put 'sales','invoiceid_10','invoice_item_product_0:name','Ice Tea telephone'

put 'sales','invoiceid_10','invoice_item_1:itemid','1'
put 'sales','invoiceid_10','invoice_item_1:invoiceid','10'
put 'sales','invoiceid_10','invoice_item_1:quantity','1'
put 'sales','invoiceid_10','invoice_item_1:cost','30'
put 'sales','invoiceid_10','invoice_item_1:productid','20'

put 'sales','invoiceid_10','invoice_item_product_1:productid','20'
put 'sales','invoiceid_10','invoice_item_product_1:price','20'
put 'sales','invoiceid_10','invoice_item_product_1:name','Ice Tea telephone'


========
get 'sales','invoiceid:10'
get 'sales', 'invoiceid:10', {COLUMN => ['invoice:total', 'invoice_customer:city']}
?get 'sales', 'invoiceid:10', {COLUMN => ['invoice:', 'invoice_customer:']}

scan 'sales', {COLUMNS => ['invoice:total']}
scan 'sales', {COLUMNS => ['invoice:total'], LIMIT => 1}
scan 'sales', {COLUMNS => 'invoice:', STARTROW => 'invoiceid:10', STOPROW => 'invoiceid:11'}

hbase -> json
========
