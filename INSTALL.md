1. create db in mysql on your machine
2. insert required data such as url, password etc. to application.yaml
3. please, use your own keys (credentials.json) for your google sheets api(https://www.youtube.com/watch?v=hVhkeGyvB04&list=PL2IQ9VnvNu0XF6DrZzsTfu52dHzQNIzRG&index=6&pp=iAQB that is the instruction on how to do that, if you need one. also you need to have permissions to add files to your google disk with this api)
4. you can put credentials.json in resources 
5. start this application and insert data in following format:
    
{  
   
"workFunctions": ["exampleFunction1", "exampleFunction2"],
   
"locations": ["exampleLocation1", "exampleLocation2"],
   
"datesToShow": ["2024-01-22", "2024-01-23"]
   
}

-all fields must be present in the request, but if you do not want to use a filter by location or date, then leave the list empty
   
-the request must contain at least one job function

-after launching the application and sending a request in Postman you will receive a link to the Google spreadsheet 
     
-you can make repeated queries and the results will be added to the previous ones, except for the same ones

-You can find a link to an example of my result in csv format in the readme.md file