var axios = require('axios');

const instance = axios.create({
    baseURL: "https://wastemanagement.westeurope.cloudapp.azure.com",
    headers:{
        "Authorization":"Basic YWRtaW46eiZwL0dOaXo"
    }
  });

const requestsToDo = 1000

for(var i = 0; i < requestsToDo; i++){ 
    const myI = i
    instance.get("/employees/current").then(res=>{
        if(res.statusCode >= 400) 
            console.log(res)
        else console.log("Request made " + myI)
    }).catch((err) => {
        console.log("An error occurred: " + err.message)
        console.log(err.response)
    })
}

