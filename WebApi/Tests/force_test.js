var axios = require('axios');

const urls = []
urls.push("/routes")
urls.push("/trucks")
urls.push("/configurations")
urls.push("/communications")
urls.push("/routes/collects")
urls.push("/routes/1/containers")
urls.push("/routes/1/collect-zones")
urls.push("/routes/1")
urls.push("/collect-zones/1")
urls.push("/containers/1")
urls.push("/configurations/1")
urls.push("/communications/2")

const instance = axios.create({
    baseURL: "https://wastemanagement.westeurope.cloudapp.azure.com",
    headers:{
        "Authorization":"Basic YWRtaW46eiZwL0dOaXo"
    }
  });

const itemsToProcess = urls.length
const requestsToDo = 5
let totalReqs = itemsToProcess * requestsToDo

console.time("force_test_completion_time")
for(var i = 0; i < requestsToDo; i++){
    const myReqNumber = i
    urls.forEach(url=>{
        console.log("Request to URL " + url)
        const timeTag = `${myReqNumber}_${instance.defaults.baseURL}${url}`
        console.time(timeTag)
        instance.get(url).then(res=>{
            console.timeEnd(myReqNumber + "_" + res.config.url)
            if(res.statusCode >= 400) 
                console.log("An error occurred in request for url " + res.config.url)
            if(--totalReqs == 0) console.timeEnd("force_test_completion_time")
        }).catch(err => {
            console.log("An error occurred: " + err.message)
            if(--totalReqs == 0) console.timeEnd("force_test_completion_time")
        })
    })
}

