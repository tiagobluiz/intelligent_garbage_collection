# Waste Management API

Esta documentação serve como referência para a API desenvolvida no âmbito do projeto **Waste Management** - Gestão da recolha de resíduos sólidos urbanos. A API encontra-se na versão v1.0.0. Os recursos detalhados nesta documentação estarão com o seu nome em inglês, visto ter sido esta a linguagem que a API usa. Note-se que apesar desta documentação estar disponível no formato PDF, aconselha-se vivamente a visualização da mesma no formato HTML, de forma a facilitar a leitura e entendimento da mesma.

## Índice

i. [Esquema](#schema)

ii. [Autenticação](#authentication)

iii. [Página inicial](#home)

iv. [Erros do cliente](#errors)

v. [Métodos HTTP](#http-verbs)

vi. [Hypermedia](#hypermedia)

vii. [Estação](#station)

viii. [Rota](#route)

ix. [Aterro de um Rota](#rou-drop-zone)

x. [Camião](#truck)

xi. [Coleta de uma Rota](#route-collection)

xii. [Configuração](#configuration)

xiii. [Comunicação](#comunication)

ixv. [Configuração de uma Comunicação](#configuration-communication)

xv. [Zonas de Coleta](#collect-zone)

xvi. [Contentor](#container)

xvii. [Coleta de um Contentor](#collect)

xviii. [Lavagem de um Contentor](#wash)

ixx. [Funcionário](#employee)

## <a name="schema"></a> Esquema
O acesso à API é realizado fazendo uso de HTTPS, através do URL `https://wastemanagement.westeurope.cloudapp.azure.com`. Todos os pedidos que envolvem corpo no pedido devem ser enviados em JSON. Todas as respostas enviadas aos clientes são derivadas de JSON, como Json-Home, Collection+JSON e Siren+JSON para respostas com sucesso, e Problem+JSON para respostas de insucesso.

Todos os campos que não tiverem valor são incluídos na representação como `null`, caso sejam objetos. Se se tratar de uma coleção, são incluídos como uma coleção vazia `[]`.

Todas as datas seguem o formato definido no ISO 8601, `YYYY-MM-DDTHH:MM:SS`.

### Representações de listas
Quando é feito um pedido a uma lista de recursos, é retornada uma resposta cuja representação vem no formato Collection+JSON (`application/vnd.collection+json`). Este formato permite representar um conjunto simples de recursos, tendo sido desenvolvido para suportar pedidos GET e POST sobre esse conjunto. Cada item da representação contem a mesma informação que a representação individual de um recurso, contudo só a partir da representação individual é possível obter as informações sobre as ações possíveis sobre um dado recurso.

Todas as representações de listas são paginadas, permitindo, através de *query parameters*, selecionar o número da página (`page`) e o número de itens que a página atual contem (`rows`). Por omissão, o valor da página é sempre 1, e o número de elementos é 20. Além deste dois parâmetros, as listas de rotas, zonas de coleta, contentores e camiões, permitem filtrar a visualização de elementos ativos e inativos, através do parâmetro `showInactive`. Se o valor for `false`, sendo este o predefinido, só serão visualizados os elementos ativos, se o valor for `true`, além dos elementos ativos, serão igualmente mostrados os inativos.

**Exemplo:** A obtenção das rotas que estão registadas em sistema, além de permitir escolher a página e o número de elementos da mesma, permite filtrar o tipo de elementos que são mostrados.

	GET /routes?page=1&rows=20&showInactive=false

### Representações individuais
Quando é feito um pedido a um recurso individual, é retornada uma resposta cuja representação vem no formato Siren+JSON (`application/vnd.siren+json`). A representação, além dos detalhes referentes ao recurso, localizados no objeto **properties**, incluí também a lista das ações que são possíveis de fazer sobre o mesmo, através do *array* **actions**, a lista de recursos relacionados, através do *array* **entities**, e a lista de *links* relacionados ao recurso, através do *array* **links**.

**Exemplo:** Obtenção de uma rota individual, neste caso aquela cujo identificador é o 1.
	
	GET /routes/1
	
Esta documentação disponibiliza um exemplo de resposta para cada método disponibilizado. Através desses exemplos será possível averiguar alguns aspetos específicos de cada recurso.

## <a name="authentication"></a> Autenticação
Quando um pedido requere autenticação, e o utilizador não tem permissões suficientes, é retornado um `403 Forbidden`. Caso não sejam fornecidas quaisquer credenciais e as mesmas sejam requeridas ou caso exista algum erro nas credenciais, quer seja por erro no nome de utilizador e/ou palavra-passe, é retornado um `401 Unauthorized`.

A API apenas disponibiliza um método de autenticação, via *header* **Basic**.

### Categorias de utilizador (*roles*)
| Nome da categoria | Descrição |
| --- | --- |
administrator | Administrador do sistema, com acesso total a todas as ações
management | Responsável pela gestão dos recursos zonas de coleta, contentores, configurações e comunicações
collector | Responsável pela recolha dos resíduos, com acesso a ações que permitam obter uma rota e marcar um contentor como recolhido e/ou lavado.

## <a name="home"></a> Página inicial
Quando é realizado um pedido à página inicial da API, na resposta vem uma representação no formato Json-Home. Este formato permite ao utilizador da API saber quais os URIs associados aos recursos que são independentes, como por exemplo, as rotas, os camiões, as estações, entre outros.

	GET https://wastemanagement.westeurope.cloudapp.azure.com/

### Resposta	
	Status: 200 OK
	Content-Type: application/json+home
	{
	    "api": {
	        "title": "Waste Management Api"
	    },
	    "resources": {
	        "/rels/route-list": {
	            "href": "/routes"
	        },
	        "/rels/station-list": {
	            "href": "/stations"
	        },
	        "/rels/configuration-list": {
	            "href": "/configurations"
	        },
	        "/rels/communication-list": {
	            "href": "/communications"
	        },
	        "/rels/truck-list": {
	            "href": "/trucks"
	        },
	        "/rels/containers-in-range": {
	            "hrefTemplate": "/containers/occupation-in-range?min={min}&max={max}"
	        },
	        "/rels/employee": {
	            "href": "/employees/current"
	        },
	        "/rels/collect-route": {
	            "hrefTemplate": "/routes/collects",
	            "hints": {
	                "allow": [
	                    "POST"
	                ],
	                "formats": {
	                    "application/json": {
	                        "latitude": -1000,
	                        "longitude": -1000,
	                        "startDate": null,
	                        "truckPlate": null,
	                        "containerType": null,
	                        "containerTypeOptions": [
	                            {
	                                "text": "General",
	                                "value": "general"
	                            },
	                            {
	                                "text": "Plastic",
	                                "value": "plastic"
	                            },
	                            {
	                                "text": "Paper",
	                                "value": "paper"
	                            },
	                            {
	                                "text": "Glass",
	                                "value": "glass"
	                            }
	                        ]
	                    }
	                },
	                "acceptPost": [
	                    "application/json"
	                ]
	            }
	        },
	        "/rels/collect-zones-in-range": {
	            "href": "/collect-zones?latitude={latitude}&longitude={longitude}&range={range}"
	        }
	    }
	}
	
### Relações
Nome | Descrição | Mais detalhes
--- | --- | ---|
/rels/route-list | Lista das rotas registadas em sistema | Ver secção [Rota](#route)
/rels/station-list | Lista da estações registadas em sistema| Ver secção [Estação](#station)
/rels/configuration-list | Lista das configurações registadas em sistema| Ver secção [Configuração](#configuration)
/rels/communication-list | Lista das comunicações registadas em sistema| Ver secção [Comunicação](#communication)
/rels/truck-list | Lista de camiões registados em sistema| Ver secção [Camião](#truck)
/rels/containers-in-range | Lista de contentores que se encontram numa dada área, tendo em conta um par de coordenadas| Ver secção [Contentor](#container)
/rels/employee | Informações sobre o funcionário atualmente autenticado| Ver secção [Funcionário](#employee)
/rels/collect-route | Recolhe uma rota selecionada pelo sistema| Ver secção [Coleta de uma Rota](#route-collection)

### Vocabulário
Nome | Tipo |Descrição | Restrições
--- | --- | --- | --- |
latitude | float | Latitude referente às coordenadas da estação | -90 =< *latitude* =< 90
longitude | float | Longitude referente às coordenadas da estação | -180 =< *longitude* =< 180
range | int | Raio, em metros, em que se vai efetuar a pesquisa por zonas de coleta | *range* >= 0
latitude | float | Latitude referente às coordenadas da localização atual do funcionário | -90 =< *latitude* =< 90
longitude | float | Longitude referente às coordenadas da localização atual do funcionário | -180 =< *longitude* =< 180
startDate | datetime | Data referente ao início da recolha | Deve cumprir o formato `YYYY-MM-DD-THH:MM:SS`
truckPlate | string | Matrícula do camião responsável pela recolha | Deve cumprir o formato `AA-BB-CC`
containerTypeOptions | - | Indicador dos tipos de contentor existentes | -

## <a name="errors"></a> Erros do cliente
Os erros, independentemente de serem originados pelo cliente ou pelo servidor, contêm sempre uma resposta consumível no formato Problem+Json(`application/problem+json`). O formato, em relação ao original, sofreu uma alteração (ver [Hypermedia - Problem+Json](#problem+json)).

Quando um cliente envia um pedido com corpo, doravante *body*, esses pedidos podem resultar em três erros:

O primeiro erro resulta do envio de um *body*, com um ou mais parâmetros inválidos. Por exemplo, na publicação de uma nova rota, os identificadores das estações de partida e chegada devem ser positivos.

	HTTP/1.1 400 Bad Request	
	{
	    "type": "/exceptions/illegal-arguments-error",
	    "title": "One or more parameters are wrong",
	    "status": 400,
	    "message": "Invalid start station identifier, it must be a positive number",
	    "detail": "Invalid start station identifier, it must be a positive number"
	}

O segundo erro resulta no envio de um *body* válido do ponto de vista lógico, mas apresenta dependências inválidas. No exemplo anterior, os identificadores seriam um número positivo, contudo, não corresponderiam a um identificador existente na base de dados.

	HTTP/1.1 422 Unprocessable Entity
	{
	    "type": "/exceptions/invalid-dependency-error",
	    "title": "Invalid dependency",
	    "status": 422,
	    "message": "Hey! One of the dependencies are invalid :/ Verify if the keys of the 
	    introduced dependencies are correct or if they even exist. More details: The identifier of
	    start or finish station is invalid",
	    "detail": "The identifier of start or finish station is invalid"
	}
	
	
Um terceiro erro resulta do envio de um *body* que não é passível de interpretação, seja porque o **Content-Type** não é `application/json`, ou porque o conteúdo, apesar de usar o formato correto, foi mal formado. Um outro exemplo, é o envio de uma data que não cumpra o formato `YYYY-MM-DDTHH:MM:SS`.

	HTTP/1.1 406 Not Acceptable
	{
	    "type": "/exceptions/not-readable-error",
	    "title": "Wrong syntax",
	    "status": 406,
	    "message": "Hey! We can't read the body as you sent us :/ Maybe you should verify if you 
	    write everything correctly",
	    "detail": "JSON parse error: "[...]
	}

## <a name="http-verbs"></a> Métodos HTTP
Na versão atual da API são usados os seguintes métodos HTTP:

| Método | Descrição |
| --- | --- |
GET | Usado para obter a representação de um recurso, não produzindo alterações de estado sobre este em nenhum caso
PUT | Usado para atualizar um recurso
POST | Usado para criar um recurso
DELETE | Usado para apagar permanentemente um recurso

## <a name="hypermedia"></a> Hypermedia
A Waste Management API usa quatro *media types* diferentes, como referido anteriormente nesta documentação. No que às respostas de sucesso diz respeito, enquanto que o Json-Home permaneceu intacto relativamente à estrutura que é definida na sua especificação[[1]](#json-home-specification), os *media types* Collection+Json[[2]](#collection-json-specification) e Siren+Json[[3]](#siren-json-specification) sofreram ambos uma alteração cirúrgica de forma a que se adaptassem melhor ao contexto onde estes eram usados. Quanto às respostas de insucesso, representadas sobre o formato Problem+Json [4]](#problem-json-specification), também sofreram uma alteração.

A alteração introduzida nas respostas de sucesso, visa solucionar uma lacuna de ambos os *media types*. Previamente à alteração, não havia qualquer maneira de indicar que um dado campo poderia assumir um conjunto fixo de valores. Por exemplo, um contentor pode ser de quatro tipos diferentes, genérico(*generic*), papel(*paper*), vidro(*glass*) e plástico(*plastic*), contudo não existia nenhuma forma de fornecer tal indicação a um cliente para que este pudesse criar ou atualizar esse recurso.
### Collection+Json
A alteração ocorreu ao nível do *array* **data**, que se encontra na composição do objeto contido no *array* **items** e na composição do objeto **template**. Além dos campos referidos na documentação, foi acrescentado um *array* denominado **options**. Este *array* é composto por um conjunto de objetos que têm um campo **text**, que indica o nome a mostrar ao utilizador e um campo **value** que refere o valor que deve ser enviado no pedido.

	{
		[...],
		"template": {
            "data": [
                [...],
                {
                    "name": "containerType",
                    "value": null,
                    "prompt": "Container Type",
                    "options": [
                        {
                            "text": "General",
                            "value": "general"
                        },
                        {
                            "text": "Plastic",
                            "value": "plastic"
                        },
                        {
                            "text": "Paper",
                            "value": "paper"
                        },
                        {
                            "text": "Glass",
                            "value": "glass"
                        }
                    ]
                },
                {
                    "name": "configurationId",
                    "value": null,
                    "prompt": "Configuration Id",
                    "options": []
                }
            ]
        }
		[...]
	}

O campo **options** só deve ser consumido caso o **value** tenha valor nulo (`null`). Se o **value** for `null` e o **options** estiver vazio, deve-se assumir o valor de **value**.

### Siren+Json
A alteração ocorreu ao nível do *array* **fields**, que se encontra na composição do objeto contido no *array* **actions**. Além dos campos referidos na documentação, e à semelhança do que foi realizado no formato Collection+Json, foi acrescentado um *array* denominado **options**. 

O campo **options** só deve ser consumido caso o **value** tenha valor nulo (`null`). Se o **value** for `null` e o **options** estiver vazio, deve-se assumir o valor de **value**.


	{
		[...],
		"actions": [
        {
            "name": "update-container-configuration",
            "title": null,
            "method": "PUT",
            "href": "/containers/1/configuration",
            "type": "application/json",
            "fields": [
               [...],
                {
                    "name": "containerType",
                    "type": "text",
                    "value": null,
                    "title": "Container Type",
                    "options": [
                        {
                            "text": "General",
                            "value": "general"
                        },
                        {
                            "text": "Plastic",
                            "value": "plastic"
                        },
                        {
                            "text": "Paper",
                            "value": "paper"
                        },
                        {
                            "text": "Glass",
                            "value": "glass"
                        }
                    ],
                    "field_class": null
                },
                {
                    "name": "configurationId",
                    "type": "number",
                    "value": null,
                    "title": "Configuration Id",
                    "options": [],
                    "field_class": null
                }
            ]
        },
        [...]
	}

### <a name="problem+json"></a> Problem+Json
Em relação à estrutura já definida na documentação, foi acrescentado o campo **message**. O objetivo desta alteração, é permitir à representação do erro separar a mensagem de erro *user-friendly* e a mensagem de erro que pode ser usada, por exemplo, para *logs* por parte da aplicação cliente, para possível despiste de erros na criação dos pedidos.

	{
	    "type": [Error identification],
	    "title": [Error name],
	    "status": [status],
	    "message": [User-friendly message],
	    "detail": [Client APP message for logging]
	}
	
### Considerações

Como é possível averiguar através da documentação do *media type* Collection+Json [[2]](#collection-json-specification), este formato permite representar uma coleção de recursos, definindo igualmente como deve ser realizada uma inserção sobre essa mesma coleção, pelo que nesta documentação, foi omitida a descrição dos pedidos referentes à inserção nas listas de recursos.

O mesmo é válido para o *media type* Siren+Json [[3]](#siren-json-specification), que através das *actions*, define quais as ações possíveis sobre um dado recurso e qual a forma de realizar essas ações, mais especificamente, o URI, o método e os campos a enviar no *body*.

No *media type* Siren+Json, os objetos que compõem o *array* **links**, que se situa no objeto retornado em primeiro nível, sendo o contexto o recurso que está a ser representado, e também nos objetos contidos no *array* **entities**, sendo o contexto a entidade referenciada, têm um campo denominado **rel**, que indica qual a relação que o *link* apresentado em **href** tem relativamente ao contexto em que está inserido. Dado que, maioritariamente, as relações usadas estão definidas em [Web Linking(RFC 5988)](https://tools.ietf.org/html/rfc5988#section-6.2) e [Link Relations](https://www.iana.org/assignments/link-relations/link-relations.xhtml), só serão documentadas as relações definidas por esta API. O *medi type* Collection+Json também tem um *array* de *links*, mantendo-se o mesmo principio referido anteriormente.
	
## <a name="station"></a>Estação
O recurso **Estação** pode ser criado/alterado por utilizadores da categoria *administrator*, e pode ser visualizado pela categoria *administrator* e *management*. O recurso é originalmente denominado **Station**.

### Lista de Estações
	
	GET /stations
	
#### Parâmetros
| Nome | Tipo | Descrição | Valor por omissão
| --- | --- | --- | --- |
page | int | Um inteiro indicativo da página a mostrar | 1
rows | int | Um inteiro indicativo do número de elementos a mostrar na página atual | 20

#### Resposta

	Status: 200 OK
	Content-Type: application/vnd.collection+json
	{
	    "collection": {
	        "version": "1.0",
	        "href": "/stations?page=1&rows=20",
	        "links": [],
	        "items": [
	            {
	                "href": "/stations/1",
	                "data": [
	                    {
	                        "name": "stationId",
	                        "value": "1",
	                        "prompt": "Station Id",
	                        "options": []
	                    },
	                    {
	                        "name": "stationName",
	                        "value": "test_station_name",
	                        "prompt": "Station Name",
	                        "options": []
	                    },
	                    {
	                        "name": "latitude",
	                        "value": "0.0",
	                        "prompt": "Latitude",
	                        "options": []
	                    },
	                    {
	                        "name": "longitude",
	                        "value": "0.0",
	                        "prompt": "Longitude",
	                        "options": []
	                    },
	                    {
	                        "name": "stationType",
	                        "value": "base",
	                        "prompt": "Station Type",
	                        "options": []
	                    }
	                ],
	                "links": []
	            }
	        ],
	        "queries": [],
	        "template": {
	            "data": [
	                {
	                    "name": "stationName",
	                    "value": null,
	                    "prompt": "Station Name",
	                    "options": []
	                },
	                {
	                    "name": "latitude",
	                    "value": null,
	                    "prompt": "Latitude",
	                    "options": []
	                },
	                {
	                    "name": "longitude",
	                    "value": null,
	                    "prompt": "Longitude",
	                    "options": []
	                },
	                {
	                    "name": "stationType",
	                    "value": null,
	                    "prompt": "Station Type",
	                    "options": [
	                        {
	                            "text": "Base",
	                            "value": "base"
	                        },
	                        {
	                            "text": "Drop Zone",
	                            "value": "drop_zone"
	                        }
	                    ]
	                }
	            ]
	        }
	    }
	}

### Estação especifica

	GET /stations/:stationId
	
#### Variáveis
| Nome | Tipo | Descrição |
| --- | --- | --- |
stationId | int | Identificador numérico da estação de que se pretende obter a representação

#### Resposta

	Status: 200 OK
	Content-Type: application/vnd.siren+json
	{
	    "class": [
	        "station"
	    ],
	    "properties": {
	        "stationId": 1,
	        "latitude": 0,
	        "longitude": 0,
	        "stationName": "test_station_name",
	        "stationType": "base"
	    },
	    "entities": [],
	    "actions": [
	        {
	            "name": "update-station",
	            "title": null,
	            "method": "PUT",
	            "href": "/stations/1",
	            "type": "application/json",
	            "fields": [
	                {
	                    "name": "stationName",
	                    "type": "text",
	                    "value": null,
	                    "title": "Station Name",
	                    "options": [],
	                    "field_class": null
	                },
	                {
	                    "name": "latitude",
	                    "type": "number",
	                    "value": null,
	                    "title": "Latitude",
	                    "options": [],
	                    "field_class": null
	                },
	                {
	                    "name": "longitude",
	                    "type": "number",
	                    "value": null,
	                    "title": "Longitude",
	                    "options": [],
	                    "field_class": null
	                },
	                {
	                    "name": "stationType",
	                    "type": "text",
	                    "value": null,
	                    "title": "Station Type",
	                    "options": [
	                        {
	                            "text": "Base",
	                            "value": "base"
	                        },
	                        {
	                            "text": "Drop Zone",
	                            "value": "drop_zone"
	                        }
	                    ],
	                    "field_class": null
	                }
	            ]
	        },
	        {
	            "name": "delete-station",
	            "title": null,
	            "method": "DELETE",
	            "href": "/stations/1",
	            "type": "*/*",
	            "fields": []
	        }
	    ],
	    "links": [
	        {
	            "rel": [
	                "self"
	            ],
	            "href": "/stations/1"
	        },
	        {
	            "rel": [
	                "up"
	            ],
	            "href": "/stations"
	        }
	    ]
	}
	
#### Classes
Nome | Descrição |
--- | --- |
station | Identifica uma estação	
	
#### Ações
Nome | Descrição |
--- | --- |
update-station | Atualiza os dados referentes a uma estação, mais concretamente a sua localização e/ou tipo
delete-station | Apaga permanentemente uma estação

### Vocabulário
Nome | Tipo |Descrição | Restrições
--- | --- | --- | --- |
stationId | int | Identificador numérico da estação | -
stationName | string | Nome da estação. Único no sistema, serve igualmente de identificador | -
latitude | float | Latitude referente às coordenadas da estação | -90 =< *latitude* =< 90
longitude | float | Longitude referente às coordenadas da estação | -180 =< *longitude* =< 180
stationType | string | Tipo da estação | *stationType* = {'base', 'drop_zone'}

## <a name="route"></a>Rota
O recurso **Rota** pode ser criado/alterado pela categoria *administrator*. A lista de Rotas pode ser visualizada pelas categorias *administrator* e *management* e uma Rota especifica pode ser visualizada pelas categorias *administrator*, *management* e *collector*. O recurso é originalmente denominado **Route**.

### Lista de Rotas
	GET /routes
	
#### Parâmetros
| Nome | Tipo | Descrição | Valor por omissão
| --- | --- | --- | --- |
page | int | Um inteiro indicativo da página a mostrar | 1
rows | int | Um inteiro indicativo do número de elementos a mostrar na página atual | 20
showInactive | boolean | Um booleano que filtra os resultados. Se tiver valor *true*, todas as rotas serão mostradas, se tiver valor *false*, só as ativas serão mostradas | false
	
#### Resposta

	Status: 200 OK
	Content-Type: application/vnd.collection+json
	{
	    "collection": {
	        "version": "1.0",
	        "href": "/routes?page=1&rows=20&showInactive=false",
	        "links": [
	            {
	                "rel": "/rels/collectable-routes",
	                "href": "/routes/collects",
	                "prompt": "Routes that are available to be collected",
	                "render": null,
	                "name": null
	            }
	        ],
	        "items": [
	            {
	                "href": "/routes/2",
	                "data": [
	                    {
	                        "name": "routeId",
	                        "value": "2",
	                        "prompt": "Route Id",
	                        "options": []
	                    },
	                    {
	                        "name": "active",
	                        "value": "true",
	                        "prompt": "Active",
	                        "options": []
	                    },
	                    {
	                        "name": "startPointStationName",
	                        "value": "test_station_name",
	                        "prompt": "Start Point Station Name",
	                        "options": []
	                    },
	                    {
	                        "name": "startPointLatitude",
	                        "value": "0.0",
	                        "prompt": "Start Point Latitude",
	                        "options": []
	                    },
	                    {
	                        "name": "startPointLongitude",
	                        "value": "0.0",
	                        "prompt": "Start Point Longitude",
	                        "options": []
	                    },
	                    {
	                        "name": "finishPointStationName",
	                        "value": "test_station_name",
	                        "prompt": "Finish Point StationName",
	                        "options": []
	                    },
	                    {
	                        "name": "finishPointLatitude",
	                        "value": "0.0",
	                        "prompt": "Finish Point Latitude",
	                        "options": []
	                    },
	                    {
	                        "name": "finishPointLongitude",
	                        "value": "0.0",
	                        "prompt": "Finish Point Longitude",
	                        "options": []
	                    }
	                ],
	                "links": []
	            }
	        ],
	        "queries": [
	            {
	                "href": "/routes?page=1&rows=20&showInactive=true",
	                "rel": "filter",
	                "prompt": null,
	                "name": null,
	                "data": []
	            }
	        ],
	        "template": {
	            "data": [
	                {
	                    "name": "startPoint",
	                    "value": null,
	                    "prompt": "Start Point",
	                    "options": []
	                },
	                {
	                    "name": "finishPoint",
	                    "value": null,
	                    "prompt": "Finish Point",
	                    "options": []
	                }
	            ]
	        }
	    }
	}

#### Relações
Nome | Descrição |
--- | --- |
/rels/collectable-routes | Referência para a lista de rotas que reunem condições para serem coletas no instante temporal em que é feito o *check* no sistema
filter | Filtrar a coleção para mostrar apenas as rotas ativas ou ativas e inativas, dependendo do valor da *flag* `showInactive`

### Rota especifica
	GET /routes/:routeId
	
#### Variáveis
| Nome | Tipo | Descrição |
| --- | --- | --- |
routeId | int | Identificador numérico da rota de que se pretende obter a representação

#### Resposta

	Status: 200 OK
	Content-Type: application/vnd.siren+json
	{
	    "class": [
	        "route"
	    ],
	    "properties": {
	        "numContainers": 1,
	        "numCollectZones": 1,
	        "numCollects": 0,
	        "routeId": 2,
	        "startPointStationName": "test_station_name",
	        "finishPointStationName": "test_station_name",
	        "active": "true",
	        "startPointLatitude": 0,
	        "startPointLongitude": 0,
	        "finishPointLatitude": 0,
	        "finishPointLongitude": 0
	    },
	    "entities": [
	        {
	            "class": [
	                "route-containers",
	                "collection"
	            ],
	            "rel": [
	                "/rels/route-containers"
	            ],
	            "href": "/routes/2/containers",
	            "properties": null,
	            "links": [],
	            "type": null,
	            "title": null
	        },
	        {
	            "class": [
	                "route-collect-zones",
	                "collection"
	            ],
	            "rel": [
	                "/rels/route-collect-zones"
	            ],
	            "href": "/routes/2/collect-zones",
	            "properties": null,
	            "links": [],
	            "type": null,
	            "title": null
	        },
	        {
	            "class": [
	                "route-collections",
	                "collection"
	            ],
	            "rel": [
	                "/rels/route-collections"
	            ],
	            "href": "/routes/2/collects",
	            "properties": null,
	            "links": [],
	            "type": null,
	            "title": null
	        },
	        {
	            "class": [
	                "route-collection-plan",
	                "collection"
	            ],
	            "rel": [
	                "/rels/route-collection-plan"
	            ],
	            "href": "/routes/2/plan",
	            "properties": null,
	            "links": [],
	            "type": null,
	            "title": null
	        },
	        {
	            "class": [
	                "route-drop-zone",
	                "collection"
	            ],
	            "rel": [
	                "/rels/route-drop-zone"
	            ],
	            "href": "/routes/2/drop-zones",
	            "properties": null,
	            "links": [],
	            "type": null,
	            "title": null
	        }
	    ],
	    "actions": [
	        {
	            "name": "update-route",
	            "title": null,
	            "method": "PUT",
	            "href": "/routes/2",
	            "type": "application/json",
	            "fields": [
	                {
	                    "name": "startPoint",
	                    "type": "number",
	                    "value": null,
	                    "title": "Start Point",
	                    "options": [],
	                    "field_class": null
	                },
	                {
	                    "name": "finishPoint",
	                    "type": "number",
	                    "value": null,
	                    "title": "Finish Point",
	                    "options": [],
	                    "field_class": null
	                }
	            ]
	        },
	        {
	            "name": "create-route-collect",
	            "title": null,
	            "method": "POST",
	            "href": "/routes/2/collects",
	            "type": "application/json",
	            "fields": [
	                {
	                    "name": "startDate",
	                    "type": "datetime-local",
	                    "value": null,
	                    "title": "Start Date",
	                    "options": [],
	                    "field_class": null
	                },
	                {
	                    "name": "truckPlate",
	                    "type": "text",
	                    "value": null,
	                    "title": "Truck Plate",
	                    "options": [],
	                    "field_class": null
	                }
	            ]
	        },
	        {
	            "name": "deactivate-route",
	            "title": null,
	            "method": "PUT",
	            "href": "/routes/2/deactivate",
	            "type": "*/*",
	            "fields": []
	        },
	        {
	            "name": "activate-route",
	            "title": null,
	            "method": "PUT",
	            "href": "/routes/2/activate",
	            "type": "*/*",
	            "fields": []
	        }
	    ],
	    "links": [
	        {
	            "rel": [
	                "self"
	            ],
	            "href": "/routes/2"
	        },
	        {
	            "rel": [
	                "up"
	            ],
	            "href": "/routes"
	        },
	        {
	            "rel": [
	                "/rels/station-list"
	            ],
	            "href": "/stations"
	        }
	    ]
	}	
	
#### Classes
Nome | Descrição |
--- | --- |
route | Identifica uma rota
route-containers | Identifica os contentores de uma rota
route-collect-zones | Identifica as zonas de coleta de uma rota
route-collections | Identifica as recolhas efetuadas sobre a rota
route-drop-zone | Identifica os aterros associados à rota

#### Relações
Nome | Descrição |
--- | --- |
/rels/route-containers | Lista de contentores pertencentes à rota
/rels/route-collect-zones | Lista de zonas de coleta associadas à rota 
/rels/route-collections | Lista de recolhas efetuadas sobre a rota
/rels/route-collection-plan | Lista de zonas de coleta que cumprem os requisitos para serem recolham e compõem a rota
/rels/route-drop-zones | Lista de aterros associados à rota
/rels/station-list | Lista de estações existentes

#### Ações
Nome | Descrição |
--- | --- |
update-route | Atualiza os pontos de partida e chegada da rota
create-route-collect | Cria uma recolha para a rota
deactivate-route | Desativa a rota
activate-route | Ativa a rota

### Vocabulário
Nome | Tipo |Descrição | Restrições
--- | --- | --- | --- |
numContainers | int | Número de contentores associados à rota | -
numCollectZones | int | Número de zonas de coleta associados à rota | -
numCollects | int | Número de recolhas efetuadas sobre a rota | -
routeId | int | Identificador numérico da rota | -
startPointStationName | string | Identificador alfanumérico da estação de partida da rota | -
finishPointStationName | string | Identificador alfanumérico da estação de chegada da rota | -
active | boolean | Indicativo do estado atual da rota, se ativa ou inativa | -
startPointLatitude | float | Latitude da estação de partida | -
startPointLongitude | float | Longitude da estação de partida | -
finishPointLatitude | float | Latitude da estação de chegada | -
finishPointLongitude | float | Longitude da estação de chegada | -
startPoint | int | Identificador numérico da estação de partida | *startPoint* >= 0
finishPoint | int | Identificador numérico da estação de chegada | *finishPoint* >= 0 
startDate | datetime | Data indicativa da hora de começo da atividade de recolha da rota | Deve cumprir o formato `YYYY-MM-DDTHH:MM:SS`
truckPlate | string | Matricula do camião responsável pela recolha | Deve cumprir o formato `AA-BB-CC`

## <a name="route-drop-zone"></a>Aterro de uma Rota
O recurso **Aterro de uma Rota** pode ser criado/alterado pela categoria *administrator* e pode ser visualizado pelas categorias *administrator* e *management*. O recurso é originalmente denominado **Route Drop Zone**.

### Lista de Aterros de uma Rota
	GET /routes/:routeId/drop-zones
	
#### Parâmetros
| Nome | Tipo | Descrição | Valor por omissão
| --- | --- | --- | --- |
page | int | Um inteiro indicativo da página a mostrar | 1
rows | int | Um inteiro indicativo do número de elementos a mostrar na página atual | 20

####	Resposta

	Status: 200 OK
	Content-Type: application/vnd.collection+json
	{
	    "collection": {
	        "version": "1.0",
	        "href": "/routes/1/drop-zones?page=1&rows=20",
	        "links": [],
	        "items": [
	            {
	                "href": "/routes/1/drop-zones",
	                "data": [
	                    {
	                        "name": "routeId",
	                        "value": "1",
	                        "prompt": "Route Id",
	                        "options": []
	                    },
	                    {
	                        "name": "dropZoneId",
	                        "value": "3",
	                        "prompt": "Drop Zone Id",
	                        "options": []
	                    },
	                    {
	                        "name": "latitude",
	                        "value": "2.0",
	                        "prompt": "Latitude",
	                        "options": []
	                    },
	                    {
	                        "name": "longitude",
	                        "value": "2.0",
	                        "prompt": "Longitude",
	                        "options": []
	                    }
	                ],
	                "links": []
	            }
	        ],
	        "queries": [],
	        "template": {
	            "data": [
	                {
	                    "name": "dropZoneId",
	                    "value": null,
	                    "prompt": "Drop Zone Id",
	                    "options": []
	                }
	            ]
	        }
	    }
	}

### Aterro de uma Rota
	GET /routes/:routeId/drop-zones/:dropZoneId
	
#### Variáveis
| Nome | Tipo | Descrição |
| --- | --- | --- |
routeId | int | Identificador numérico da rota
dropZoneId | int | Identificador numérico da estação/aterro

#### Resposta

	Status: 200 OK
	Content-Type: application/vnd.siren+json
	{
	    "class": [
	        "route-drop-zone"
	    ],
	    "properties": {
	        "routeId": 1,
	        "dropZoneId": 3,
	        "latitude": 2,
	        "longitude": 2
	    },
	    "entities": [
	        {
	            "class": [
	                "route"
	            ],
	            "rel": [
	                "/rels/route"
	            ],
	            "href": "/routes/1",
	            "properties": null,
	            "links": [],
	            "type": null,
	            "title": null
	        }
	    ],
	    "actions": [
	        {
	            "name": "delete-route-drop-zone",
	            "title": null,
	            "method": "DELETE",
	            "href": "/routes/1/drop-zones",
	            "type": "*/*",
	            "fields": []
	        }
	    ],
	    "links": [
	        {
	            "rel": [
	                "self"
	            ],
	            "href": "/routes/1/drop-zones/3"
	        },
	        {
	            "rel": [
	                "up"
	            ],
	            "href": "/routes/1/drop-zones"
	        }
	    ]
	}
	
#### Classes
Nome | Descrição |
--- | --- |
route-drop-zone | Identifica um aterro associado a uma rota
route | Identifica uma rota

#### Relações
Nome | Descrição |
--- | --- |
/rels/route | Rota a que o aterro está associado
	
#### Ações
Nome | Descrição |
--- | --- |
delete-route-drop-zone | Retira a associação do aterro à rota	
	
### Vocabulário
Nome | Tipo |Descrição | Restrições
--- | --- | --- | --- |
routeId | int | Identificador numérico da rota | -
dropZoneId | int | Identificador numérico da estação/aterro | *dropZoneId* >= 0
latitude | float | Latitude referente às coordenadas da estação | -
longitude | float | Longitude referente às coordenadas da estação | -	

## <a name="truck"></a>Camião
O recurso **Camião** pode ser criado/alterado pela categoria *administrator* e pode ser visualizado pelas categorias *administrator* e *management*. O recurso é originalmente denominado **Truck**.

### Lista de Camiões
	GET /trucks

#### Parâmetros
| Nome | Tipo | Descrição | Valor por omissão
| --- | --- | --- | --- |
page | int | Um inteiro indicativo da página a mostrar | 1
rows | int | Um inteiro indicativo do número de elementos a mostrar na página atual | 20
showInactive | boolean | Um booleano que filtra os resultados. Se tiver valor *true*, todos os camiões serão mostrados, se tiver valor *false*, só os ativos serão mostrados | false

#### Resposta

	Status: 200 OK
	Content-Type: application/vnd.collection+json
	{
	    "collection": {
	        "version": "1.0",
	        "href": "/trucks?page=1&rows=20&showInactive=false",
	        "links": [],
	        "items": [
	            {
	                "href": "/trucks",
	                "data": [
	                    {
	                        "name": "truckPlate",
	                        "value": "06-BG-33",
	                        "prompt": "Truck Plate",
	                        "options": []
	                    },
	                    {
	                        "name": "active",
	                        "value": "false",
	                        "prompt": "Active",
	                        "options": []
	                    }
	                ],
	                "links": []
	            }
	        ],
	        "queries": [
	            {
	                "href": "/trucks?page=1&rows=20&showInactive=true",
	                "rel": "filter",
	                "prompt": null,
	                "name": null,
	                "data": []
	            }
	        ],
	        "template": {
	            "data": [
	                {
	                    "name": "truckPlate",
	                    "value": null,
	                    "prompt": "Truck Plate",
	                    "options": []
	                }
	            ]
	        }
	    }
	}
	

####Relações
Nome | Descrição |
--- | --- |
filter | Filtrar a coleção para mostrar apenas os camiões ativos ou ativos e inativos, dependendo do valor da *flag* `showInactive`	
	
### Camião especifico
	GET /trucks/:truckPlate
#### Variáveis
| Nome | Tipo | Descrição |
| --- | --- | --- |
truckPlate | string | Matrícula do camião de que se pretende obter a representação

#### Resposta

	Status: 200 OK
	Content-Type: application/vnd.siren+json
	{
	    "class": [
	        "truck"
	    ],
	    "properties": {
	        "registrationPlate": "06-BG-33",
	        "active": "false"
	    },
	    "entities": [
	        {
	            "class": [
	                "truck-collects",
	                "collection"
	            ],
	            "rel": [
	                "/rels/truck-collects"
	            ],
	            "href": "/trucks/06-BG-33/collects",
	            "properties": null,
	            "links": [],
	            "type": null,
	            "title": null
	        }
	    ],
	    "actions": [
	        {
	            "name": "deactivate-truck",
	            "title": null,
	            "method": "PUT",
	            "href": "/trucks/deactivate",
	            "type": "*/*",
	            "fields": []
	        },
	        {
	            "name": "activate-truck",
	            "title": null,
	            "method": "PUT",
	            "href": "/trucks/activate",
	            "type": "*/*",
	            "fields": []
	        }
	    ],
	    "links": [
	        {
	            "rel": [
	                "self"
	            ],
	            "href": "/trucks/06-BG-33"
	        },
	        {
	            "rel": [
	                "up"
	            ],
	            "href": "/trucks"
	        }
	    ]
	}
	
#### Classes
Nome | Descrição |
--- | --- |
truck | Identifica um camião
truck-collects | Identifica as recolhas efetuadas pelo camião

#### Relações
Nome | Descrição |
--- | --- |
/rels/truck-collects | Lista com as recolhas efetuadas pelo camião

#### Ações
Nome | Descrição |
--- | --- |
deactivate-truck | Desativa um camião
activate-truck | Ativa um camião

### Vocabulário
Nome | Tipo | Descrição | Restrições
--- | --- | --- | --- |
registrationPlate | string | Matrícula do camião | Deve cumprir o formato `AA-BB-CC`
active | boolean | Indicativo do estado atual do camião, se ativo ou inativo | -

## <a name="route-collection"></a>Coleta de uma Rota

###Tabela de recursos
Nome original do recurso| URI | Descrição | Autorizações
--- | --- | --- | --- |
Collectable Routes | /routes/collects | Lista de rotas que podem ser recolhidas | Criar: *administrator*, *management*, *collector*
Route Collections List | /routes/:routeId/collects | Lista de recolhas efetuadas sobre uma rota | Criar/atualizar/visualizar: *administrator*, *managament*
Route Collection | /routes/:routeId/collects/:startDate | Recurso representativo de uma recolha efetuada sobre uma rota | Criar/atualizar/visualizar: *administrator*, *management*
Truck Collections List | /trucks/:truckPlate/collects | Coletas efetuadas por um dado camião | Visualizar: *administrator*, *management*
Route Collection Plan | /routes/:routeId/plan | Lista de zonas de coleta que cumprem os requisitos para serem recolham e compõem a rota | Visualizar: *administrator*, *management*, *collector*

A exposição dos recursos encontra-se pela ordem da tabela acima.

### Lista de rotas disponíveis para recolha 
	GET /route/collects
	
#### Parâmetros
| Nome | Tipo | Descrição | Valor por omissão
| --- | --- | --- | --- |
page | int | Um inteiro indicativo da página a mostrar | 1
rows | int | Um inteiro indicativo do número de elementos a mostrar na página atual | 20	
#### Resposta

	Status: 200 OK
	Content-Type: application/vnd.collection+json
	{
	    "collection": {
	        "version": "1.0",
	        "href": "/routes/collects?page=1&rows=20&type=paper",
	        "links": [],
	        "items": [
	            {
	                "href": "/routes/1",
	                "data": [
	                    {
	                        "name": "routeId",
	                        "value": "1",
	                        "prompt": "Route Id",
	                        "options": []
	                    },
	                    {
	                        "name": "active",
	                        "value": "true",
	                        "prompt": "Active",
	                        "options": []
	                    },
	                    {
	                        "name": "startPoint",
	                        "value": "4",
	                        "prompt": "Start Point",
	                        "options": []
	                    },
	                    {
	                        "name": "finishPoint",
	                        "value": "5",
	                        "prompt": "Finish Point",
	                        "options": []
	                    }
	                ],
	                "links": [
	                    {
	                        "rel": "/rels/route-collections",
	                        "href": "/routes/1/collects",
	                        "prompt": "Get route collections",
	                        "render": null,
	                        "name": null
	                    }
	                ]
	            }
	        ],
	        "queries": [],
	        "template": {
	            "data": [
	                {
	                    "name": "latitude",
	                    "value": null,
	                    "prompt": "Latitude",
	                    "options": []
	                },
	                {
	                    "name": "longitude",
	                    "value": null,
	                    "prompt": "Longitude",
	                    "options": []
	                },
	                {
	                    "name": "startDate",
	                    "value": null,
	                    "prompt": "Start Date",
	                    "options": []
	                },
	                {
	                    "name": "truckPlate",
	                    "value": null,
	                    "prompt": "Truck Plate",
	                    "options": []
	                },
	                {
	                    "name": "containerType",
	                    "value": null,
	                    "prompt": "Container Type",
	                    "options": [
	                        {
	                            "text": "General",
	                            "value": "general"
	                        },
	                        {
	                            "text": "Plastic",
	                            "value": "plastic"
	                        },
	                        {
	                            "text": "Paper",
	                            "value": "paper"
	                        },
	                        {
	                            "text": "Glass",
	                            "value": "glass"
	                        }
	                    ]
	                }
	            ]
	        }
	    }
	}
	
#### Relações
Nome | Descrição |
--- | --- |
/rels/route-collections | Lista de recolhas efetuadas sobre uma rota

###Vocabulário
Nome | Tipo |Descrição | Restrições
--- | --- | --- | --- |
latitude | float | Latitude referente às coordenadas da localização atual do funcionário | -90 =< *latitude* =< 90
longitude | float | Longitude referente às coordenadas da localização atual do funcionário | -180 =< *longitude* =< 180
startDate | datetime | Data referente ao início da recolha | Deve cumprir o formato `YYYY-MM-DD-THH:MM:SS`
truckPlate | string | Matrícula do camião responsável pela recolha | Deve cumprir o formato `AA-BB-CC`

### Lista de Coletas de uma Rota
	GET /route/:routeId/collects
	
#### Variáveis
| Nome | Tipo | Descrição |
| --- | --- | --- |
routeId | int | Identificador numérico da rota

#### Parâmetros
| Nome | Tipo | Descrição | Valor por omissão
| --- | --- | --- | --- |
page | int | Um inteiro indicativo da página a mostrar | 1
rows | int | Um inteiro indicativo do número de elementos a mostrar na página atual | 20

#### Resposta

	Status: 200 OK
	Content-Type: application/vnd.collection+json
	{
	    "collection": {
	        "version": "1.0",
	        "href": "/routes/1/collects?page=1&rows=20",
	        "links": [],
	        "items": [
	            {
	                "href": "/routes/1/collects/2018-05-05T20:56:32",
	                "data": [
	                    {
	                        "name": "routeId",
	                        "value": "1",
	                        "prompt": "Route Id",
	                        "options": []
	                    },
	                    {
	                        "name": "startDate",
	                        "value": "2018-05-05T20:56:32",
	                        "prompt": "Start Date",
	                        "options": []
	                    },
	                    {
	                        "name": "finishDate",
	                        "value": null,
	                        "prompt": "Finish Date",
	                        "options": []
	                    },
	                    {
	                        "name": "truckPlate",
	                        "value": "06-BG-33",
	                        "prompt": "Truck Plate",
	                        "options": []
	                    }
	                ],
	                "links": []
	            }
	        ],
	        "queries": [],
	        "template": {
	            "data": [
	                {
	                    "name": "startDate",
	                    "value": null,
	                    "prompt": "Start Date",
	                    "options": []
	                },
	                {
	                    "name": "truckPlate",
	                    "value": null,
	                    "prompt": "Truck Plate",
	                    "options": []
	                }
	            ]
	        }
	    }
	}
			
### Coleta de uma Rota especifica
	GET /routes/:routeId/collects/:startDate
	
#### Variáveis
| Nome | Tipo | Descrição |
| --- | --- | --- |
routeId | int | Identificador numérico da rota
startDate | string | Data referente ao inicio do ato de recolha da rota

#### Resposta

	Status: 200 OK
	Content-Type: application/vnd.siren+json
	{
	    "class": [
	        "route-collections"
	    ],
	    "properties": {
	        "routeId": 1,
	        "truckPlate": "06-BG-33",
	        "startDate": [
	            2018,
	            5,
	            5,
	            20,
	            56,
	            32
	        ],
	        "finishDate": null
	    },
	    "entities": [
	        {
	            "class": [
	                "route-collection-plan",
	                "collection"
	            ],
	            "rel": [
	                "/rels/route-collection-plan"
	            ],
	            "href": "/routes/1/plan",
	            "properties": null,
	            "links": [],
	            "type": null,
	            "title": null
	        }
	    ],
	    "actions": [
	        {
	            "name": "update-route-collection",
	            "title": null,
	            "method": "PUT",
	            "href": "/routes/1/collects/2018-05-05T20:56:32",
	            "type": "application/json",
	            "fields": [
	                {
	                    "name": "finishDate",
	                    "type": "datetime-local",
	                    "value": null,
	                    "title": "Finish Date",
	                    "options": [],
	                    "field_class": null
	                },
	                {
	                    "name": "truckPlate",
	                    "type": "text",
	                    "value": null,
	                    "title": "Truck Plate",
	                    "options": [],
	                    "field_class": null
	                }
	            ]
	        }
	    ],
	    "links": [
	        {
	            "rel": [
	                "up"
	            ],
	            "href": "/routes/1"
	        },
	        {
	            "rel": [
	                "self"
	            ],
	            "href": "/routes/1/collects/2018-05-05T20:56:32"
	        }
	    ]
	}

#### Classes
Nome | Descrição |
--- | --- |
route-collections | Coleta efetuada sobre uma rota
route-collection-plan | Identifica o plano de recolha da rota

#### Relações
Nome | Descrição |
--- | --- |
/rels/route-collection-plan | Lista de zonas de coleta que cumprem os requisitos para serem recolham e compõem a rota
	
#### Ações
Nome | Descrição |
--- | --- |
update-route-collection | Atualiza o recurso, mais concretamente, a data de finalização da recolha e o camião que realizou a recolha

### Lista de Coletas de um Camião
	GET /trucks/:truckPlate/collects
	
#### Variáveis
| Nome | Tipo | Descrição |
| --- | --- | --- |
truckPlate | Matrícula do camião

#### Parâmetros
| Nome | Tipo | Descrição | Valor por omissão
| --- | --- | --- | --- |
page | int | Um inteiro indicativo da página a mostrar | 1
rows | int | Um inteiro indicativo do número de elementos a mostrar na página atual | 20

#### Resposta
	
	Status: 200 OK
	Content-Type: application/vnd.collection+json
	{
	    "collection": {
	        "version": "1.0",
	        "href": "/trucks/06-BG-33/collects?page=1&rows=20",
	        "links": [],
	        "items": [
	            {
	                "href": "/routes/1/collects/2018-05-05T20:56:32",
	                "data": [
	                    {
	                        "name": "routeId",
	                        "value": "1",
	                        "prompt": "Route Id",
	                        "options": []
	                    },
	                    {
	                        "name": "startDate",
	                        "value": "2018-05-05T20:56:32",
	                        "prompt": "Start Date",
	                        "options": []
	                    },
	                    {
	                        "name": "finishDate",
	                        "value": null,
	                        "prompt": "Finish Date",
	                        "options": []
	                    },
	                    {
	                        "name": "truckPlate",
	                        "value": "06-BG-33",
	                        "prompt": "Truck Plate",
	                        "options": []
	                    }
	                ],
	                "links": []
	            }
	        ],
	        "queries": [],
	        "template": {
	            "data": [
	                {
	                    "name": "startDate",
	                    "value": null,
	                    "prompt": "Start Date",
	                    "options": []
	                },
	                {
	                    "name": "truckPlate",
	                    "value": "06-BG-33",
	                    "prompt": "Truck Plate",
	                    "options": []
	                }
	            ]
	        }
	    }
	}

### Vocabulário
Nome | Tipo |Descrição | Restrições
--- | --- | --- | --- |
routeId | int | Identificador numérico da rota | -
truckPlate | string | Matrícula do camião que efetuou a recolha | Deve cumprir o formato `AA-BB-CC`
startDate | datetime | Data referente ao inicio do ato de recolha da rota | Deve cumprir o formato `YYYY-MM-DD-THH:MM:SS`
finishDate | datetime | Data referente ao término do ato de recolha da rota | Deve cumprir o formato `YYYY-MM-DD-THH:MM:SS`

### Plano de Recolha da Rota
	GET /routes/:routeId/plan	
	
#### Variáveis
| Nome | Tipo | Descrição |
| --- | --- | --- |
routeId | string | Identificador numérico da rota

#### Parâmetros
| Nome | Tipo | Descrição | Valor por omissão
| --- | --- | --- | --- |
page | int | Um inteiro indicativo da página a mostrar | 1
rows | int | Um inteiro indicativo do número de elementos a mostrar na página atual | 20	
### Resposta

	Status: 200 OK
	Content-Type: application/vnd.collection+json
	{
	    "collection": {
	        "version": "1.0",
	        "href": "/routes/2/plan?page=1&rows=20",
	        "links": [],
	        "items": [
	            {
	                "href": "/collect-zones/1",
	                "data": [
	                    {
	                        "name": "collectZoneId",
	                        "value": "1",
	                        "prompt": "Collect Zone Id",
	                        "options": []
	                    },
	                    {
	                        "name": "routeId",
	                        "value": "2",
	                        "prompt": "Route Id",
	                        "options": []
	                    },
	                    {
	                        "name": "pickOrder",
	                        "value": "32767",
	                        "prompt": "Pick Order",
	                        "options": []
	                    },
	                    {
	                        "name": "active",
	                        "value": "true",
	                        "prompt": "Active",
	                        "options": []
	                    },
	                    {
	                        "name": "latitude",
	                        "value": "38.6318",
	                        "prompt": "Latitude",
	                        "options": []
	                    },
	                    {
	                        "name": "longitude",
	                        "value": "-9.144287",
	                        "prompt": "Longitude",
	                        "options": []
	                    }
	                ],
	                "links": [
	                    {
	                        "rel": "/rels/collect-collect-zone-containers",
	                        "href": "/collect-zones/1/collects",
	                        "prompt": null,
	                        "render": null,
	                        "name": null
	                    },
	                    {
	                        "rel": "/rels/wash-collect-zone-containers",
	                        "href": "/collect-zones/1/washes",
	                        "prompt": null,
	                        "render": null,
	                        "name": null
	                    }
	                ]
	            }
	        ],
	        "queries": [],
	        "template": null
	    }
	}

#### Relações
Nome | Descrição |
--- | --- |
/rels/collect-collect-zone-containers | Recolhe todos os contentores de um dado tipo na zona de coleta
/rels/wash-collect-zone-containers | Lava todos os contentores de um dado tipo na zona de coleta

### Vocabulário
Nome | Tipo |Descrição | Restrições
--- | --- | --- | --- |
collectZoneId | int | Identificador numérico da zona de coleta | -
routeId | int | Identificador numérico da rota | -
pickOrder | int | Identificador da ordem em que a zona de coleta deve ser recolhida | -
active | boolean | Indicativo do estado atual da rota, se ativa ou inativa | -
latitude | float | Latitude referente às coordenadas da estação | -
longitude | float | Longitude referente às coordenadas da estação | -	
## <a name="configuration"></a>Configuração
O recurso **Configuração** pode ser criado/alterado/visualizado pelas categorias *administrator* e *management*. O recurso é originalmente denominado **Configuration**.

### Lista de Configurações
	GET /configurations
	
#### Parâmetros
| Nome | Tipo | Descrição | Valor por omissão
| --- | --- | --- | --- |
page | int | Um inteiro indicativo da página a mostrar | 1
rows | int | Um inteiro indicativo do número de elementos a mostrar na página atual | 20	
#### Resposta
	Status: 200 OK
	Content-Type: application/vnd.collection+json
	{
	    "collection": {
	        "version": "1.0",
	        "href": "/configurations?page=1&rows=20",
	        "links": [],
	        "items": [
	            {
	                "href": "/configurations/1",
	                "data": [
	                    {
	                        "name": "configurationId",
	                        "value": "1",
	                        "prompt": "Configuration Id",
	                        "options": []
	                    },
	                    {
	                        "name": "configurationName",
	                        "value": "Test One",
	                        "prompt": "Configuration Name",
	                        "options": []
	                    }
	                ],
	                "links": []
	            }
	        ],
	        "queries": [],
	        "template": {
	            "data": [
	                {
	                    "name": "configurationName",
	                    "value": null,
	                    "prompt": "Configuration Name",
	                    "options": []
	                }
	            ]
	        }
	    }
	}
	
### Configuração especifica
	GET /configurations/:configurationId
	
#### Variáveis
| Nome | Tipo | Descrição |
| --- | --- | --- |
configurationId | int | Identificador numérico da configuração

#### Resposta
	Status: 200 OK
	Content-Type: application/vnd.siren+json
	{
	    "class": [
	        "configuration"
	    ],
	    "properties": {
	        "configurationId": 1,
	        "configurationName": "Test One"
	    },
	    "entities": [
	        {
	            "class": [
	                "configuration-communication-list",
	                "collection"
	            ],
	            "rel": [
	                "/rels/configuration-communication-list"
	            ],
	            "href": "/configurations/1/communications",
	            "properties": null,
	            "links": [],
	            "type": null,
	            "title": null
	        }
	    ],
	    "actions": [
	        {
	            "name": "update-configuration",
	            "title": null,
	            "method": "PUT",
	            "href": "/configurations/1",
	            "type": "application/json",
	            "fields": [
	                {
	                    "name": "configurationName",
	                    "type": "text",
	                    "value": null,
	                    "title": "Configuration Name",
	                    "options": [],
	                    "field_class": null
	                }
	            ]
	        },
	        {
	            "name": "delete-configuration",
	            "title": null,
	            "method": "DELETE",
	            "href": "/configurations/1",
	            "type": "*/*",
	            "fields": []
	        }
	    ],
	    "links": [
	        {
	            "rel": [
	                "self"
	            ],
	            "href": "/configurations/1"
	        },
	        {
	            "rel": [
	                "up"
	            ],
	            "href": "/configurations"
	        }
	    ]
	}	

#### Classes
Nome | Descrição |
--- | --- |
configuration | Identifica uma configuração
configuration-communication-list | Comunicações associadas à configuração

####Relações
Nome | Descrição |
--- | --- |
/rels/configuration-communication-list	| Lista de comunicações associadas à configuração
	
#### Ações
Nome | Descrição |
--- | --- |
update-configuration | Atualiza o nome da configuração
delete-configuration | Apaga a configuração

### Vocabulário
Nome | Tipo |Descrição | Restrições
--- | --- | --- | --- |
configurationId | int | Identificador numérico da configuração | -
configurationName | string | Identificador alfanumérico da configuração | -

## <a name="communication"></a>Comunicação
O recurso **Comunicação** pode ser criado/alterado/visualizado pelas categorias *administrator* e *management*. O recurso é originalmente denominado **Communication**.

### Lista de Comunicações
	GET /communications
	
#### Parâmetros
| Nome | Tipo | Descrição | Valor por omissão
| --- | --- | --- | --- |
page | int | Um inteiro indicativo da página a mostrar | 1
rows | int | Um inteiro indicativo do número de elementos a mostrar na página atual | 20

#### Resposta
	Status: 200 OK
	Content-Type: application/vnd.collection+json
	{
	    "collection": {
	        "version": "1.0",
	        "href": "/communications?page=1&rows=20",
	        "links": [],
	        "items": [
	            {
	                "href": "/communications/2",
	                "data": [
	                    {
	                        "name": "communicationId",
	                        "value": "2",
	                        "prompt": "Communication Id",
	                        "options": []
	                    },
	                    {
	                        "name": "communicationDesignation",
	                        "value": "max_threshold",
	                        "prompt": "Communication Designation",
	                        "options": []
	                    }
	                ],
	                "links": []
	            },
	            {
	                "href": "/communications/3",
	                "data": [
	                    {
	                        "name": "communicationId",
	                        "value": "3",
	                        "prompt": "Communication Id",
	                        "options": []
	                    },
	                    {
	                        "name": "communicationDesignation",
	                        "value": "max_temperature",
	                        "prompt": "Communication Designation",
	                        "options": []
	                    }
	                ],
	                "links": []
	            },
	            {
	                "href": "/communications/4",
	                "data": [
	                    {
	                        "name": "communicationId",
	                        "value": "4",
	                        "prompt": "Communication Id",
	                        "options": []
	                    },
	                    {
	                        "name": "communicationDesignation",
	                        "value": "communication_time_interval",
	                        "prompt": "Communication Designation",
	                        "options": []
	                    }
	                ],
	                "links": []
	            },
	            {
	                "href": "/communications/5",
	                "data": [
	                    {
	                        "name": "communicationId",
	                        "value": "5",
	                        "prompt": "Communication Id",
	                        "options": []
	                    },
	                    {
	                        "name": "communicationDesignation",
	                        "value": "collected_max_threshold",
	                        "prompt": "Communication Designation",
	                        "options": []
	                    }
	                ],
	                "links": []
	            }
	        ],
	        "queries": [],
	        "template": {
	            "data": [
	                {
	                    "name": "communicationDesignation",
	                    "value": null,
	                    "prompt": "Communication Designation",
	                    "options": []
	                }
	            ]
	        }
	    }
	}
	
**Observações:**	Nesta representação estão incluídas as quatro comunicações predefinidas.

### Comunicação especifica
	GET /communication/:communicationId
	
#### Variáveis
| Nome | Tipo | Descrição |
| --- | --- | --- |
communicationId | int | Identificador numérico da comunicação

#### Resposta
	Status: 200 OK
	Content-Type: application/vnd.siren+json
	{
	    "class": [
	        "communication"
	    ],
	    "properties": {
	        "communicationId": 2,
	        "communicationDesignation": "max_threshold"
	    },
	    "entities": [],
	    "actions": [
	        {
	            "name": "update-communication",
	            "title": null,
	            "method": "PUT",
	            "href": "/communications/2",
	            "type": "application/json",
	            "fields": [
	                {
	                    "name": "communicationDesignation",
	                    "type": "text",
	                    "value": null,
	                    "title": "Communication Designation",
	                    "options": [],
	                    "field_class": null
	                }
	            ]
	        },
	        {
	            "name": "delete-communication",
	            "title": null,
	            "method": "DELETE",
	            "href": "/communications/2",
	            "type": "*/*",
	            "fields": []
	        }
	    ],
	    "links": [
	        {
	            "rel": [
	                "self"
	            ],
	            "href": "/communications/2"
	        },
	        {
	            "rel": [
	                "up"
	            ],
	            "href": "/communications"
	        }
	    ]
	}
	
#### Classes
Nome | Descrição |
--- | --- |
communication | Identifica uma comunicação
	
#### Ações
Nome | Descrição |
--- | --- |
update-communication | Atualiza o nome associado à comunicação
delete-communication | Apaga uma comunicação

### Vocabulário
Nome | Tipo |Descrição | Restrições
--- | --- | --- | --- |
communicationId | int | Identificador numérico da comunicação | -
communicationDesignation | string | Identificador alfanumérico da comunicação | -

## <a name="configuration-communication"></a>Comunicação de uma Configuração
O recurso **Comunicação de uma Configuração** pode ser criado/alterado/visualizado pelas categorias *administrator* e *management*. O recurso é originalmente denominado **Configuration Communication**.

### Lista de Comunicações de uma Configuração
	GET /configurations/:configurationId/communications
	
#### Variáveis
| Nome | Tipo | Descrição |
| --- | --- | --- |
configurationId | int | Identificador numérico da configuração

#### Resposta
	Status: 200 OK
	Content-Type: application/vnd.collection+json
	{
	    "collection": {
	        "version": "1.0",
	        "href": "/configurations/1/communications?page=1&rows=20",
	        "links": [],
	        "items": [
	            {
	                "href": "/configurations/1/communications/2",
	                "data": [
	                    {
	                        "name": "configurationId",
	                        "value": "1",
	                        "prompt": "Configuration Id",
	                        "options": []
	                    },
	                    {
	                        "name": "communicationId",
	                        "value": "2",
	                        "prompt": "Communication Id",
	                        "options": []
	                    },
	                    {
	                        "name": "communicationDesignation",
	                        "value": "max_threshold",
	                        "prompt": "Communication Designation",
	                        "options": []
	                    },
	                    {
	                        "name": "value",
	                        "value": "70",
	                        "prompt": "Value",
	                        "options": []
	                    }
	                ],
	                "links": []
	            },
	            {
	                "href": "/configurations/1/communications/4",
	                "data": [
	                    {
	                        "name": "configurationId",
	                        "value": "1",
	                        "prompt": "Configuration Id",
	                        "options": []
	                    },
	                    {
	                        "name": "communicationId",
	                        "value": "4",
	                        "prompt": "Communication Id",
	                        "options": []
	                    },
	                    {
	                        "name": "communicationDesignation",
	                        "value": "communication_time_interval",
	                        "prompt": "Communication Designation",
	                        "options": []
	                    },
	                    {
	                        "name": "value",
	                        "value": "1",
	                        "prompt": "Value",
	                        "options": []
	                    }
	                ],
	                "links": []
	            },
	            {
	                "href": "/configurations/1/communications/5",
	                "data": [
	                    {
	                        "name": "configurationId",
	                        "value": "1",
	                        "prompt": "Configuration Id",
	                        "options": []
	                    },
	                    {
	                        "name": "communicationId",
	                        "value": "5",
	                        "prompt": "Communication Id",
	                        "options": []
	                    },
	                    {
	                        "name": "communicationDesignation",
	                        "value": "collected_max_threshold",
	                        "prompt": "Communication Designation",
	                        "options": []
	                    },
	                    {
	                        "name": "value",
	                        "value": "15",
	                        "prompt": "Value",
	                        "options": []
	                    }
	                ],
	                "links": []
	            }
	        ],
	        "queries": [],
	        "template": {
	            "data": [
	                {
	                    "name": "communicationId",
	                    "value": null,
	                    "prompt": "Communication Id",
	                    "options": []
	                },
	                {
	                    "name": "value",
	                    "value": null,
	                    "prompt": "Value",
	                    "options": []
	                }
	            ]
	        }
	    }
	}

### Comunicação de uma Configuração especifica
		GET /configurations/:configurationId/communications/:communicationId
	
#### Variáveis
| Nome | Tipo | Descrição |
| --- | --- | --- |
configurationId | int | Identificador numérico da configuração
communicationId | int | Identificador numérico da comunicação	
#### Resposta
	Status: 200 OK
	Content-Type: application/vnd.siren+json
	{
	    "class": [
	        "configuration-communication"
	    ],
	    "properties": {
	        "configurationId": 1,
	        "communicationId": 2,
	        "value": 70,
	        "communicationDesignation": "max_threshold"
	    },
	    "entities": [
	        {
	            "class": [
	                "communication"
	            ],
	            "rel": [
	                "/rels/communication"
	            ],
	            "href": "/communications/2",
	            "properties": null,
	            "links": [],
	            "type": null,
	            "title": null
	        },
	        {
	            "class": [
	                "configuration"
	            ],
	            "rel": [
	                "/rels/configuration"
	            ],
	            "href": "/configurations/1",
	            "properties": null,
	            "links": [],
	            "type": null,
	            "title": null
	        }
	    ],
	    "actions": [
	        {
	            "name": "disassociate-communication-configuration",
	            "title": null,
	            "method": "DELETE",
	            "href": "/configurations/1/communications/2",
	            "type": "*/*",
	            "fields": []
	        }
	    ],
	    "links": [
	        {
	            "rel": [
	                "self"
	            ],
	            "href": "/configurations/1/communications/2"
	        },
	        {
	            "rel": [
	                "up"
	            ],
	            "href": "/configurations/1/communications"
	        }
	    ]
	}
	
#### Classes
Nome | Descrição |
--- | --- |
configuration-communication | Identifica uma comunicação associada a uma configuração
communication | Identifica uma comunicação
configuration | Identifica uma configuração

####Relações
Nome | Descrição |
--- | --- |
/rels/communication | Identifica uma dada comunicação
/rels/configuration | Identifica uma dada configuração	
#### Ações
Nome | Descrição |
--- | --- |
disassociate-communication-configuration | Retira a associação existente entre a comunicação e a configuração

### Vocabulário
Nome | Tipo |Descrição | Restrições
--- | --- | --- | --- |
configurationId | int | Identificador numérico da configuração | -
communicationId | int | Identificador numérico da comunicação | -
communicationDesignation | string | Identificador alfanumérico da comunicação | -
value | int | Valor associado à comunicação | *value* >= -15	
## <a name="collect-zone"></a>Zonas de Coleta
O recurso **Zona de Coleta** pode ser criado/alterado/visualizado pelas categorias *administrator* e *management*. O recurso é originalmente denominado **Collect Zone**.

### Lista de Zonas de Coleta
	GET /routes/:routeId/collect-zones
	
#### Variáveis
| Nome | Tipo | Descrição |
| --- | --- | --- |
routeId | int | Identificador numérico da rota

#### Parâmetros
| Nome | Tipo | Descrição | Valor por omissão
| --- | --- | --- | --- |
page | int | Um inteiro indicativo da página a mostrar | 1
rows | int | Um inteiro indicativo do número de elementos a mostrar na página atual | 20


#### Resposta
	Status: 200 OK
	Content-Type: application/vnd.collection+json
	{
	    "collection": {
	        "version": "1.0",
	        "href": "/routes/1/collect-zones?page=1&rows=20&showInactive=false",
	        "links": [],
	        "items": [
	            {
	                "href": "/collect-zones/1",
	                "data": [
	                    {
	                        "name": "collectZoneId",
	                        "value": "1",
	                        "prompt": "Collect Zone Id",
	                        "options": []
	                    },
	                    {
	                        "name": "routeId",
	                        "value": "1",
	                        "prompt": "Route Id",
	                        "options": []
	                    },
	                    {
	                        "name": "pickOrder",
	                        "value": "32767",
	                        "prompt": "Pick Order",
	                        "options": []
	                    },
	                    {
	                        "name": "active",
	                        "value": "true",
	                        "prompt": "Active",
	                        "options": []
	                    },
	                    {
	                        "name": "latitude",
	                        "value": "38.756393",
	                        "prompt": "Latitude",
	                        "options": []
	                    },
	                    {
	                        "name": "longitude",
	                        "value": "-9.113894",
	                        "prompt": "Longitude",
	                        "options": []
	                    }
	                ],
	                "links": []
	            }
	        ],
	        "queries": [
	            {
	                "href": "/routes/1/collect-zones?page=1&rows=20&showInactive=true",
	                "rel": "filter",
	                "prompt": null,
	                "name": null,
	                "data": []
	            }
	        ],
	        "template": {
	            "data": [
	                {
	                    "name": "routeId",
	                    "value": "1",
	                    "prompt": "Route Id",
	                    "options": []
	                }
	            ]
	        }
	    }
	}
	
####Relações
Nome | Descrição |
--- | --- |
filter | Filtrar a coleção para mostrar apenas os zonas de coleta ativas ou ativas e inativas, dependendo do valor da *flag* `showInactive`

###  <a name="collect-zones-in-range"></a> Lista de Zonas de Coleta numa dada área
	GET /collect-zones
	
#### Parâmetros
| Nome | Tipo | Descrição | Valor por omissão
| --- | --- | --- | --- |
latitude | float | Latitude referente às coordenadas do ponto central ou localização atual | -
longitude | float | Longitude  referente às coordenadas do ponto central ou localização atual | -
range | int | Raio referente à area em que vai ser efetuada a procura por zonas de coleta | 25	
#### Resposta
	Status: 200 OK
	Content-Type: application/vnd.collection+json
	{
	    "collection": {
	        "version": "1.0",
	        "href": "/collect-zones?latitude=38.756393&longitude=-9.113894&range=25",
	        "links": [],
	        "items": [
	            {
	                "href": "/collect-zones/1",
	                "data": [
	                    {
	                        "name": "collectZoneId",
	                        "value": "1",
	                        "prompt": "Collect Zone Id",
	                        "options": []
	                    },
	                    {
	                        "name": "routeId",
	                        "value": "1",
	                        "prompt": "Route Id",
	                        "options": []
	                    },
	                    {
	                        "name": "pickOrder",
	                        "value": "32767",
	                        "prompt": "Pick Order",
	                        "options": []
	                    },
	                    {
	                        "name": "active",
	                        "value": "true",
	                        "prompt": "Active",
	                        "options": []
	                    },
	                    {
	                        "name": "latitude",
	                        "value": "38.756393",
	                        "prompt": "Latitude",
	                        "options": []
	                    },
	                    {
	                        "name": "longitude",
	                        "value": "-9.113894",
	                        "prompt": "Longitude",
	                        "options": []
	                    }
	                ],
	                "links": []
	            },
	            {
	                "href": "/collect-zones/7",
	                "data": [
	                    {
	                        "name": "collectZoneId",
	                        "value": "7",
	                        "prompt": "Collect Zone Id",
	                        "options": []
	                    },
	                    {
	                        "name": "routeId",
	                        "value": "3",
	                        "prompt": "Route Id",
	                        "options": []
	                    },
	                    {
	                        "name": "pickOrder",
	                        "value": "6",
	                        "prompt": "Pick Order",
	                        "options": []
	                    },
	                    {
	                        "name": "active",
	                        "value": "true",
	                        "prompt": "Active",
	                        "options": []
	                    },
	                    {
	                        "name": "latitude",
	                        "value": "38.756397",
	                        "prompt": "Latitude",
	                        "options": []
	                    },
	                    {
	                        "name": "longitude",
	                        "value": "-9.113896",
	                        "prompt": "Longitude",
	                        "options": []
	                    }
	                ],
	                "links": []
	            }
	        ],
	        "queries": [],
	        "template": null
	    }
	}

### Zona de Coleta especifica
	GET /collect-zones/:collectZoneId
	
#### Variáveis
| Nome | Tipo | Descrição |
| --- | --- | --- |
collectZoneId | int | Identificador numérico da zona de coleta

#### Resposta
	Status: 200 OK
	Content-Type: application/vnd.siren+json
	{
	    "class": [
	        "collect-zone"
	    ],
	    "properties": {
	        "collectZoneId": 1,
	        "routeId": 1,
	        "pickOrder": 32767,
	        "numContainers": 1,
	        "active": "true",
	        "latitude": 38.756393,
	        "longitude": -9.113894,
	        "generalOccupation": 0,
	        "plasticOccupation": 89,
	        "paperOccupation": 0,
	        "glassOccupation": 0
	    },
	    "entities": [
	        {
	            "class": [
	                "collect-zone-containers",
	                "collection"
	            ],
	            "rel": [
	                "/rels/collect-zone-containers"
	            ],
	            "href": "/collect-zones/1/containers",
	            "properties": null,
	            "links": [],
	            "type": null,
	            "title": null
	        }
	    ],
	    "actions": [
	        {
	            "name": "update-collect-zone",
	            "title": null,
	            "method": "PUT",
	            "href": "/collect-zones/1",
	            "type": "application/json",
	            "fields": [
	                {
	                    "name": "routeId",
	                    "type": "text",
	                    "value": null,
	                    "title": "Route Id",
	                    "options": [],
	                    "field_class": null
	                }
	            ]
	        },
	        {
	            "name": "deactivate-collect-zone",
	            "title": null,
	            "method": "PUT",
	            "href": "/collect-zones/1/deactivate",
	            "type": "*/*",
	            "fields": []
	        },
	        {
	            "name": "activate-collect-zone",
	            "title": null,
	            "method": "PUT",
	            "href": "/collect-zones/1/activate",
	            "type": "*/*",
	            "fields": []
	        }
	    ],
	    "links": [
	        {
	            "rel": [
	                "self"
	            ],
	            "href": "/collect-zones/1"
	        },
	        {
	            "rel": [
	                "/rels/route-list"
	            ],
	            "href": "/routes"
	        },
	        {
	            "rel": [
	                "up"
	            ],
	            "href": "/routes/1"
	        }
	    ]
	}
	
#### Classes
Nome | Descrição |
--- | --- |
collect-zone | Identifica uma Zona de Coleta
collect-zone-containers | Identifica os Contentores de uma Zona de Coleta


####Relações
Nome | Descrição |
--- | --- |
/rels/collect-zone-containers | Lista de contentores de uma zona de coleta
/rels/route-list | Lista de rotas existentes no sistema
	
#### Ações
Nome | Descrição |
--- | --- |
update-collect-zone | Atualiza uma zona de coleta
activate-collect-zone | Ativa a zona de coleta
deactivate-collect-zone | Desativa a zona de coleta


### Vocabulário
Nome | Tipo |Descrição | Restrições
--- | --- | --- | --- |
collectZoneId | int | Identificador numérico da zona de coleta | -
routeId | int | Identificador numérico da rota a associar à zona de coleta | *routeId* >= 0
pickOrder | int | Ordem de recolha da zona de coleta | -
numContainers | int | Número de contentores associados à zona de coleta | -
active | boolean | Indicativo do estado atual da zona de coleta, se ativa ou inativa | -
latitude | float | Latitude referente às coordenadas da zona de coleta | -90 =< *latitude* =< 90
longitude | float | Longitude referente às coordenadas da zona de coleta | -180 =< *longitude* =< 180
generalOccupation | int | Ocupação mais elevada referente aos contentores do tipo comum, originalmente denominados *general* | -
plasticOccupation | int | Ocupação mais elevada referente aos contentores do tipo plástico, originalmente denominados *plastic* | -
paperOccupation | int | Ocupação mais elevada referente aos contentores do tipo papel, originalmente denominados *paper* | -
glassOccupation | int | Ocupação mais elevada referente aos contentores do tipo vidro, originalmente denominados *glass* | -

## <a name="container"></a>Contentor
O recurso **Contentor** pode ser criado/alterado/visualizado pelas categorias *administrator* e *management*. O recurso é originalmente denominado **Container**.

### Lista de Contentores de uma Zona de Coleta
	GET /collect-zones/:collectZoneId/containers
	
#### Variáveis
| Nome | Tipo | Descrição |
| --- | --- | --- |
collectZoneId | int | Identificador numérico da zona de coleta

#### Parâmetros
| Nome | Tipo | Descrição | Valor por omissão
| --- | --- | --- | --- |
page | int | Um inteiro indicativo da página a mostrar | 1
rows | int | Um inteiro indicativo do número de elementos a mostrar na página atual | 20

#### Resposta
	Status: 200 OK
	Content-Type: application/vnd.collection+json
	{
	    "collection": {
	        "version": "1.0",
	        "href": "/collect-zones/1/containers?page=1&rows=20&showInactive=false",
	        "links": [],
	        "items": [
	            {
	                "href": "/containers/1",
	                "data": [
	                    {
	                        "name": "containerId",
	                        "value": "1",
	                        "prompt": "Container Id",
	                        "options": []
	                    },
	                    {
	                        "name": "iotId",
	                        "value": "1D18AE",
	                        "prompt": "Iot Id",
	                        "options": []
	                    },
	                    {
	                        "name": "active",
	                        "value": "true",
	                        "prompt": "Active",
	                        "options": []
	                    },
	                    {
	                        "name": "latitude",
	                        "value": "38.756393",
	                        "prompt": "Latitude",
	                        "options": []
	                    },
	                    {
	                        "name": "longitude",
	                        "value": "-9.113894",
	                        "prompt": "Longitude",
	                        "options": []
	                    },
	                    {
	                        "name": "height",
	                        "value": "100",
	                        "prompt": "Latitude",
	                        "options": []
	                    },
	                    {
	                        "name": "containerType",
	                        "value": "paper",
	                        "prompt": "Container Type",
	                        "options": []
	                    },
	                    {
	                        "name": "lastReadDate",
	                        "value": "2018-07-09T10:19:07.780",
	                        "prompt": "Last Read Date",
	                        "options": []
	                    },
	                    {
	                        "name": "battery",
	                        "value": "-1",
	                        "prompt": "Battery",
	                        "options": []
	                    },
	                    {
	                        "name": "occupation",
	                        "value": "89",
	                        "prompt": "Occupation",
	                        "options": []
	                    },
	                    {
	                        "name": "temperature",
	                        "value": "26",
	                        "prompt": "Temperature",
	                        "options": []
	                    },
	                    {
	                        "name": "collectZoneId",
	                        "value": "1",
	                        "prompt": "Collect Zone Id",
	                        "options": []
	                    },
	                    {
	                        "name": "configurationId",
	                        "value": "1",
	                        "prompt": "Configuration Id",
	                        "options": []
	                    }
	                ],
	                "links": []
	            }
	        ],
	        "queries": [
	            {
	                "href": "/collect-zones/1/containers?page=1&rows=20&showInactive=true",
	                "rel": "filter",
	                "prompt": null,
	                "name": null,
	                "data": []
	            }
	        ],
	        "template": {
	            "data": [
	                {
	                    "name": "iotId",
	                    "value": null,
	                    "prompt": "Iot Id",
	                    "options": []
	                },
	                {
	                    "name": "latitude",
	                    "value": null,
	                    "prompt": "Latitude",
	                    "options": []
	                },
	                {
	                    "name": "longitude",
	                    "value": null,
	                    "prompt": "Longitude",
	                    "options": []
	                },
	                {
	                    "name": "height",
	                    "value": null,
	                    "prompt": "Height",
	                    "options": []
	                },
	                {
	                    "name": "containerType",
	                    "value": null,
	                    "prompt": "Container Type",
	                    "options": [
	                        {
	                            "text": "General",
	                            "value": "general"
	                        },
	                        {
	                            "text": "Plastic",
	                            "value": "plastic"
	                        },
	                        {
	                            "text": "Paper",
	                            "value": "paper"
	                        },
	                        {
	                            "text": "Glass",
	                            "value": "glass"
	                        }
	                    ]
	                },
	                {
	                    "name": "configurationId",
	                    "value": null,
	                    "prompt": "Configuration Id",
	                    "options": []
	                }
	            ]
	        }
	    }
	}	

#### Relações
Nome | Descrição |
--- | --- |
filter | Filtrar a coleção para mostrar apenas os contentores ativos ou ativos e inativos, dependendo do valor da *flag* `showInactive`

### Lista de Contentores de uma Rota
	GET /routes/:routeId/containers
	
#### Variáveis
| Nome | Tipo | Descrição |
| --- | --- | --- |
routeId | int | Identificador numérico da rota

#### Parâmetros
| Nome | Tipo | Descrição | Valor por omissão
| --- | --- | --- | --- |
page | int | Um inteiro indicativo da página a mostrar | 1
rows | int | Um inteiro indicativo do número de elementos a mostrar na página atual | 20

#### Resposta
	Status: 200 OK
	Content-Type: application/vnd.collection+json
	{
	    "collection": {
	        "version": "1.0",
	        "href": "/routes/1/containers?page=1&rows=20&showInactive=false",
	        "links": [],
	        "items": [
	            {
	                "href": "/containers/1",
	                "data": [
	                    {
	                        "name": "containerId",
	                        "value": "1",
	                        "prompt": "Container Id",
	                        "options": []
	                    },
	                    {
	                        "name": "iotId",
	                        "value": "1D18AE",
	                        "prompt": "Iot Id",
	                        "options": []
	                    },
	                    {
	                        "name": "active",
	                        "value": "true",
	                        "prompt": "Active",
	                        "options": []
	                    },
	                    {
	                        "name": "latitude",
	                        "value": "38.756393",
	                        "prompt": "Latitude",
	                        "options": []
	                    },
	                    {
	                        "name": "longitude",
	                        "value": "-9.113894",
	                        "prompt": "Longitude",
	                        "options": []
	                    },
	                    {
	                        "name": "height",
	                        "value": "100",
	                        "prompt": "Latitude",
	                        "options": []
	                    },
	                    {
	                        "name": "containerType",
	                        "value": "paper",
	                        "prompt": "Container Type",
	                        "options": []
	                    },
	                    {
	                        "name": "lastReadDate",
	                        "value": "2018-07-09T10:19:07.780",
	                        "prompt": "Last Read Date",
	                        "options": []
	                    },
	                    {
	                        "name": "battery",
	                        "value": "-1",
	                        "prompt": "Battery",
	                        "options": []
	                    },
	                    {
	                        "name": "occupation",
	                        "value": "89",
	                        "prompt": "Occupation",
	                        "options": []
	                    },
	                    {
	                        "name": "temperature",
	                        "value": "26",
	                        "prompt": "Temperature",
	                        "options": []
	                    },
	                    {
	                        "name": "collectZoneId",
	                        "value": "1",
	                        "prompt": "Collect Zone Id",
	                        "options": []
	                    },
	                    {
	                        "name": "configurationId",
	                        "value": "1",
	                        "prompt": "Configuration Id",
	                        "options": []
	                    }
	                ],
	                "links": []
	            }
	        ],
	        "queries": [
	            {
	                "href": "/routes/1/containers?page=1&rows=20&showInactive=true",
	                "rel": "filter",
	                "prompt": null,
	                "name": null,
	                "data": []
	            }
	        ],
	        "template": null
	    }
	}
	
#### Relações
Nome | Descrição |
--- | --- |
filter | Filtrar a coleção para mostrar apenas os contentores ativos ou ativos e inativos, dependendo do valor da *flag* `showInactive`

### Contentor especifico
	GET /containers/:containerId

#### Variáveis
| Nome | Tipo | Descrição |
| --- | --- | --- |
containerId | int | Identificador numérico do contentor

#### Resposta
	Status: 200 OK
	Content-Type: application/vnd.siren+json
	{
	    "class": [
	        "container"
	    ],
	    "properties": {
	        "numCollects": 3,
	        "numWashes": 3,
	        "containerId": 1,
	        "collectZoneId": 1,
	        "configurationId": 1,
	        "latitude": 38.756393,
	        "longitude": -9.113894,
	        "height": 100,
	        "battery": -1,
	        "temperature": 26,
	        "occupation": 89,
	        "containerType": "paper",
	        "iotId": "1D18AE",
	        "active": "true",
	        "lastReadDate": "2018-07-09T10:19:07.780"
	    },
	    "entities": [
	        {
	            "class": [
	                "configuration"
	            ],
	            "rel": [
	                "/rels/configuration"
	            ],
	            "href": "/configurations/1",
	            "properties": null,
	            "links": [],
	            "type": null,
	            "title": null
	        },
	        {
	            "class": [
	                "collect-zone"
	            ],
	            "rel": [
	                "/rels/collect-zone"
	            ],
	            "href": null,
	            "properties": null,
	            "links": [
	                {
	                    "rel": [
	                        "self"
	                    ],
	                    "href": "/collect-zones/1"
	                }
	            ],
	            "type": null,
	            "title": null
	        },
	        {
	            "class": [
	                "wash",
	                "collection"
	            ],
	            "rel": [
	                "/rels/wash"
	            ],
	            "href": "/containers/1/washes",
	            "properties": null,
	            "links": [],
	            "type": null,
	            "title": null
	        },
	        {
	            "class": [
	                "collect",
	                "collection"
	            ],
	            "rel": [
	                "/rels/collect"
	            ],
	            "href": "/containers/1/collects",
	            "properties": null,
	            "links": [],
	            "type": null,
	            "title": null
	        }
	    ],
	    "actions": [
	        {
	            "name": "update-container-configuration",
	            "title": null,
	            "method": "PUT",
	            "href": "/containers/1/configuration",
	            "type": "application/json",
	            "fields": [
	                {
	                    "name": "iotId",
	                    "type": "text",
	                    "value": null,
	                    "title": "Iot Id",
	                    "options": [],
	                    "field_class": null
	                },
	                {
	                    "name": "height",
	                    "type": "number",
	                    "value": null,
	                    "title": "Height",
	                    "options": [],
	                    "field_class": null
	                },
	                {
	                    "name": "containerType",
	                    "type": "text",
	                    "value": null,
	                    "title": "Container Type",
	                    "options": [
	                        {
	                            "text": "General",
	                            "value": "general"
	                        },
	                        {
	                            "text": "Plastic",
	                            "value": "plastic"
	                        },
	                        {
	                            "text": "Paper",
	                            "value": "paper"
	                        },
	                        {
	                            "text": "Glass",
	                            "value": "glass"
	                        }
	                    ],
	                    "field_class": null
	                },
	                {
	                    "name": "configurationId",
	                    "type": "number",
	                    "value": null,
	                    "title": "Configuration Id",
	                    "options": [],
	                    "field_class": null
	                }
	            ]
	        },
	        {
	            "name": "update-container-localization",
	            "title": null,
	            "method": "PUT",
	            "href": "/containers/1/localization",
	            "type": "application/json",
	            "fields": [
	                {
	                    "name": "collectZoneId",
	                    "type": "number",
	                    "value": null,
	                    "title": "Collect Zone Id",
	                    "options": [],
	                    "field_class": null
	                },
	                {
	                    "name": "latitude",
	                    "type": "number",
	                    "value": null,
	                    "title": "Latitude",
	                    "options": [],
	                    "field_class": null
	                },
	                {
	                    "name": "longitude",
	                    "type": "number",
	                    "value": null,
	                    "title": "Longitude",
	                    "options": [],
	                    "field_class": null
	                }
	            ]
	        },
	        {
	            "name": "update-container-reads",
	            "title": null,
	            "method": "PUT",
	            "href": "/containers/1/reads",
	            "type": "application/json",
	            "fields": [
	                {
	                    "name": "battery",
	                    "type": "number",
	                    "value": null,
	                    "title": "Battery",
	                    "options": [],
	                    "field_class": null
	                },
	                {
	                    "name": "temperature",
	                    "type": "number",
	                    "value": null,
	                    "title": "Temperature",
	                    "options": [],
	                    "field_class": null
	                },
	                {
	                    "name": "occupation",
	                    "type": "number",
	                    "value": null,
	                    "title": "Occupation",
	                    "options": [],
	                    "field_class": null
	                }
	            ]
	        },
	        {
	            "name": "deactivate-container",
	            "title": null,
	            "method": "PUT",
	            "href": "/containers/1/deactivate",
	            "type": "*/*",
	            "fields": []
	        },
	        {
	            "name": "activate-container",
	            "title": null,
	            "method": "PUT",
	            "href": "/containers/1/activate",
	            "type": "*/*",
	            "fields": []
	        }
	    ],
	    "links": [
	        {
	            "rel": [
	                "self"
	            ],
	            "href": "/containers/1"
	        },
	        {
	            "rel": [
	                "/rels/configuration-list"
	            ],
	            "href": "/configurations"
	        },
	        {
	            "rel": [
	                "/rels/collect-zones-in-range"
	            ],
	            "href": "/collect-zones?latitude=38.756393&longitude=-9.113894"
	        }
	    ]
	}	

#### Classes
Nome | Descrição |
--- | --- |
container | Identifica um Contentor
configuration | Identifica uma Configuração
collect-zone | Identifica uma Zona de Coleta
wash | Identifica uma Lavagem
collect | Identifica uma Coleta

####Relações
Nome | Descrição |
--- | --- |
/rels/configuration | Configuração associada ao contentor
/rels/configuration-list | Lista com todas as configurações registadas no sistema
/rels/collect-zone | Zona de coleta associada ao contentor
/rels/wash | Lista de lavagens efetuadas sobre o contentor
/rels/collect | Lista de coletas efetuadas sobre o contentor
/rels/collect-zones-in-range | Lista de zonas de coleta numa dada área (ver [ Lista de Zonas de Coleta numa dada área](#collect-zones-in-range))
	
#### Ações
Nome | Descrição |
--- | --- |
update-container-configuration | Atualiza os dados personalizáveis do contentor
update-container-localization | Altera a localização do contentor e/ou a zona de coleta
update-container-reads | Atualiza as leituras associadas ao contentor
deactivate-container | Desativa o contentor
activate-container | Ativa o contentor

### Contentores com ocupação compreendida numa dada extensão
	GET /containers/occupation-in-range
	
#### Parâmetros
| Nome | Tipo | Descrição | Valor por omissão
| --- | --- | --- | --- |	
min | int | Ocupação minima, inclusiva | -
max | int | Ocupação máxima, inclusiva | -

#### Resposta
	Status: 200 OK
	Content-Type: application/vnd.siren+json
	{
	    "class": [
	        "containers-in-range"
	    ],
	    "properties": {
	        "occupationInRange": 14.29
	    },
	    "entities": [],
	    "actions": [],
	    "links": [
	        {
	            "rel": [
	                "self"
	            ],
	            "href": "/containers/occupation-in-range?min=80&max=100"
	        }
	    ]
	}
	
#### Classes
Nome | Descrição |
--- | --- |
containers-in-range | Identifica a percentagem de contentores com ocupação compreendida numa dada extensão	
	
### Contentores de uma Rota com ocupação compreendida numa dada extensão
	GET /routes/:routeId/containers/occupation-in-range
	
#### Variáveis
| Nome | Tipo | Descrição |
| --- | --- | --- |
routeId | int | Identificador numérico da rota

	
#### Resposta
	Status: 200 OK
	Content-Type: application/vnd.siren+json
	{
	    "class": [
	        "containers-in-range",
	        "route"
	    ],
	    "properties": {
	        "occupationInRange": 14.29
	    },
	    "entities": [],
	    "actions": [],
	    "links": [
	        {
	            "rel": [
	                "self"
	            ],
	            "href": "/routes/1/containers/occupation-in-range?min=80&max=100"
	        },
	        {
	            "rel": [
	                "up"
	            ],
	            "href": "/routes/1"
	        }
	    ]
	}	

#### Classes
Nome | Descrição |
--- | --- |

containers-in-range | Identifica a percentagem de contentores com ocupação compreendida numa dada extensão	
route | Rota a ser inquirida

### Vocabulário
Nome | Tipo |Descrição | Restrições
--- | --- | --- | --- |
numCollects | int | Número de coletas efetuadas sobre o contentor | -
numWashes | int | Número de lavagens efetuadas sobre o contentor | -
containerId | Identificador numérico do contentor | -
collectZoneId | int | Identificador numérico da zona de coleta associada ao contentor | *collectZoneId* >= 0
configurationId | int | Identificador numérico da configuração associada ao contentor | * configurationId* >= 0
latitude | float | Latitude referente às coordenadas da zona de coleta | -90 =< *latitude* =< 90
longitude | float | Longitude referente às coordenadas da zona de coleta | -180 =< *longitude* =< 180
height | short | Altura do contentor | *height* > 0
battery | short | Bateria do dispositivo instalado no contentor | *battery* >= -1
temperature | short | Temperatura ambiente do contentor | *temperature* >= -15
occupation | short | Ocupação do contentor | *occupation* >= -1
containerType | string | Tipo do contentor | *containerType* = {*general, glass, paper, plastic*}
iotId | string | Identificador alfanumérico do dispositivo instalado no contentor | -
lastReadDate | datetime | Data da receção da última leitura efetuada pelo dispositivo |-
active | boolean | Indicativo do estado atual da rota, se ativa ou inativa | -
occupationInRange | float | Valor percentual dos contentores com ocupação compreendida numa dada extensão 

## <a name="collect"></a>Coleta de um Contentor
O recurso **Coleta de um Contentor** pode ser criado pelas categorias *administrator*, *management* e *collector*, podendo ser alterado e visualizado apenas pelas primeiras duas. O recurso é originalmente denominado **Collect**.

### Lista de Coletas de um Contentor
	GET /containers/:containerId/collects
	
#### Variáveis
| Nome | Tipo | Descrição |
| --- | --- | --- |
containerId | int | Identificador numérico do contentor

#### Parâmetros
| Nome | Tipo | Descrição | Valor por omissão
| --- | --- | --- | --- |
page | int | Um inteiro indicativo da página a mostrar | 1
rows | int | Um inteiro indicativo do número de elementos a mostrar na página atual | 20	

#### Resposta
	Status: 200 OK
	Content-Type: application/vnd.collection+json
	{
	    "collection": {
	        "version": "1.0",
	        "href": "/containers/1/collects?page=1&rows=20",
	        "links": [],
	        "items": [
	            {
	                "href": "/containers/1/collects/2018-07-12T20:58:58",
	                "data": [
	                    {
	                        "name": "containerId",
	                        "value": "1",
	                        "prompt": "Container Id",
	                        "options": []
	                    },
	                    {
	                        "name": "collectDate",
	                        "value": "2018-07-12T20:58:58",
	                        "prompt": "Collect Date",
	                        "options": []
	                    },
	                    {
	                        "name": "confirmed",
	                        "value": "true",
	                        "prompt": "Confirmed",
	                        "options": []
	                    }
	                ],
	                "links": []
	            },
	            {
	                "href": "/containers/1/collects/2018-07-08T20:58:58",
	                "data": [
	                    {
	                        "name": "containerId",
	                        "value": "1",
	                        "prompt": "Container Id",
	                        "options": []
	                    },
	                    {
	                        "name": "collectDate",
	                        "value": "2018-07-08T20:58:58",
	                        "prompt": "Collect Date",
	                        "options": []
	                    },
	                    {
	                        "name": "confirmed",
	                        "value": "false",
	                        "prompt": "Confirmed",
	                        "options": []
	                    }
	                ],
	                "links": []
	            }
	        ],
	        "queries": [],
	        "template": {
	            "data": [
	                {
	                    "name": "collectDate",
	                    "value": null,
	                    "prompt": "Collect Date",
	                    "options": []
	                }
	            ]
	        }
	    }
	}
	
### Coleta de um Contentor
	GET /containers/:containerId/collects/:collectDate
	
#### Variáveis
| Nome | Tipo | Descrição |
| --- | --- | --- |
containerId | int | Identificador numérico do contentor
collectDate | datetime | Data de realização da coleta

#### Resposta
	Status: 200 OK
	Content-Type: application/vnd.siren+json
	{
	    "class": [
	        "collect"
	    ],
	    "properties": {
	        "containerId": 1,
	        "collectDate": [
	            2018,
	            7,
	            12,
	            20,
	            58,
	            58
	        ],
	        "confirmed": "true"
	    },
	    "entities": [],
	    "actions": [
	        {
	            "name": "update-collect",
	            "title": null,
	            "method": "PUT",
	            "href": "/containers/1/collects/2018-07-12T20:58:58",
	            "type": "application/json",
	            "fields": [
	                {
	                    "name": "collectDate",
	                    "type": "datetime-local",
	                    "value": null,
	                    "title": "Collect Date",
	                    "options": [],
	                    "field_class": null
	                }
	            ]
	        }
	    ],
	    "links": [
	        {
	            "rel": [
	                "up"
	            ],
	            "href": "/containers/1"
	        },
	        {
	            "rel": [
	                "self"
	            ],
	            "href": "/containers/1/collects/2018-07-12T20:58:58"
	        }
	    ]
	}
	
#### Classes
Nome | Descrição |
--- | --- |
collect | Identifica uma Coleta de um Contentor
	
#### Ações
Nome | Descrição |
--- | --- |
update-collect | Atualiza uma coleta de um contentor

### Vocabulário
Nome | Tipo |Descrição | Restrições
--- | --- | --- | --- |
containerId | int | Identificador numérico do contentor | -
collectDate | datetime | Data de realização da coleta | -
confirmed | boolean | Indicativo da confirmação, por parte do dispositivo, da realização da coleta		
		
## <a name="wash"></a>Lavagem
O recurso **Contentor** pode ser criado pelas categorias *administrator*, *management* e *collector*, podendo ser alterado e visualizado apenas pelas primeiras duas. O recurso é originalmente denominado **Collect**.		
### Lista de Lavagens de um Contentor
	GET /containers/:containerId/washes
	
#### Variáveis
| Nome | Tipo | Descrição |
| --- | --- | --- |
containerId | int | Identificador numérico do contentor

#### Parâmetros
| Nome | Tipo | Descrição | Valor por omissão
| --- | --- | --- | --- |
page | int | Um inteiro indicativo da página a mostrar | 1
rows | int | Um inteiro indicativo do número de elementos a mostrar na página atual | 20	

#### Resposta
	Status: 200 OK
	Content-Type: application/vnd.collection+json
	{
	    "collection": {
	        "version": "1.0",
	        "href": "/containers/1/washes?page=1&rows=20",
	        "links": [],
	        "items": [
	            {
	                "href": "/containers/1/washes/1997-11-11T08:40",
	                "data": [
	                    {
	                        "name": "containerId",
	                        "value": "1",
	                        "prompt": "Container Id",
	                        "options": []
	                    },
	                    {
	                        "name": "washDate",
	                        "value": "1997-11-11T08:40",
	                        "prompt": "Wash Date",
	                        "options": []
	                    }
	                ],
	                "links": []
	            }
	        ],
	        "queries": [],
	        "template": {
	            "data": [
	                {
	                    "name": "washDate",
	                    "value": null,
	                    "prompt": "Wash Date",
	                    "options": []
	                }
	            ]
	        }
	    }
	}
	
### Lavagem de um Contentor especifica
	GET /containers/:containerId/washes/:washDate
	
#### Variáveis
| Nome | Tipo | Descrição |
| --- | --- | --- |
containerId | int | Identificador numérico do contentor
washDate | datetime | Data de realização da lavagem

#### Resposta
	Status: 200 OK
	Content-Type: application/vnd.siren+json
	{
	    "class": [
	        "wash"
	    ],
	    "properties": {
	        "containerId": 1,
	        "washDate": [
	            1997,
	            11,
	            11,
	            8,
	            40
	        ]
	    },
	    "entities": [],
	    "actions": [
	        {
	            "name": "update-wash",
	            "title": null,
	            "method": "PUT",
	            "href": "/containers/1/washes/1997-11-11T08:40",
	            "type": "application/json",
	            "fields": [
	                {
	                    "name": "washDate",
	                    "type": "datetime-local",
	                    "value": null,
	                    "title": "Wash Date",
	                    "options": [],
	                    "field_class": null
	                }
	            ]
	        }
	    ],
	    "links": [
	        {
	            "rel": [
	                "self"
	            ],
	            "href": "/containers/1/washes/1997-11-11T08:40"
	        },
	        {
	            "rel": [
	                "up"
	            ],
	            "href": "/containers/1/washes"
	        }
	    ]
	}

#### Classes
Nome | Descrição |
--- | --- |
wash | Identifica uma Lavagem de um Contentor
	
#### Ações
Nome | Descrição |
--- | --- |
update-wash | Atualiza uma lavagem de um contentor

### Vocabulário
Nome | Tipo |Descrição | Restrições
--- | --- | --- | --- |
containerId | int | Identificador numérico do contentor | -
washDate | datetime | Data de realização da lavagem | -	
##<a name="employee"></a> Funcionário
O recurso **Funcionário** pode ser criado/alterado/visualizado pela categoria *administrator*. Todas as categorias podem aceder à informação que o sistema tem para o utilizador que está autenticado no ato do pedido. O recurso é originalmente denominado **Employee**.

### Lista de Funcionários
	GET /employees
	
#### Parâmetros
| Nome | Tipo | Descrição | Valor por omissão
| --- | --- | --- | --- |
page | int | Um inteiro indicativo da página a mostrar | 1
rows | int | Um inteiro indicativo do número de elementos a mostrar na página atual | 20	
#### Resposta
	Status: 200 OK
	Content-Type: application/vnd.collection+json
	{
	    "collection": {
	        "version": "1.0",
	        "href": "/employees?page=1&rows=20",
	        "links": [],
	        "items": [
	            {
	                "href": "/employees/admin",
	                "data": [
	                    {
	                        "name": "username",
	                        "value": "admin",
	                        "prompt": "Username",
	                        "options": []
	                    },
	                    {
	                        "name": "name",
	                        "value": "Administrator",
	                        "prompt": "Name",
	                        "options": []
	                    },
	                    {
	                        "name": "email",
	                        "value": "therealadmin@wastecollecter.com",
	                        "prompt": "Email",
	                        "options": []
	                    },
	                    {
	                        "name": "phoneNumber",
	                        "value": "912388798",
	                        "prompt": "Phone number",
	                        "options": []
	                    },
	                    {
	                        "name": "job",
	                        "value": "administrator",
	                        "prompt": "Job",
	                        "options": []
	                    }
	                ],
	                "links": []
	            },
	            {
	                "href": "/employees/collector_user",
	                "data": [
	                    {
	                        "name": "username",
	                        "value": "collector_user",
	                        "prompt": "Username",
	                        "options": []
	                    },
	                    {
	                        "name": "name",
	                        "value": "Collector",
	                        "prompt": "Name",
	                        "options": []
	                    },
	                    {
	                        "name": "email",
	                        "value": "therealcollector@wastecollecter.com",
	                        "prompt": "Email",
	                        "options": []
	                    },
	                    {
	                        "name": "phoneNumber",
	                        "value": "912388898",
	                        "prompt": "Phone number",
	                        "options": []
	                    },
	                    {
	                        "name": "job",
	                        "value": "collector",
	                        "prompt": "Job",
	                        "options": []
	                    }
	                ],
	                "links": []
	            },
	            {
	                "href": "/employees/management_user",
	                "data": [
	                    {
	                        "name": "username",
	                        "value": "management_user",
	                        "prompt": "Username",
	                        "options": []
	                    },
	                    {
	                        "name": "name",
	                        "value": "Management",
	                        "prompt": "Name",
	                        "options": []
	                    },
	                    {
	                        "name": "email",
	                        "value": "therealmanagement@wastecollecter.com",
	                        "prompt": "Email",
	                        "options": []
	                    },
	                    {
	                        "name": "phoneNumber",
	                        "value": "912388998",
	                        "prompt": "Phone number",
	                        "options": []
	                    },
	                    {
	                        "name": "job",
	                        "value": "management",
	                        "prompt": "Job",
	                        "options": []
	                    }
	                ],
	                "links": []
	            }
	        ],
	        "queries": [],
	        "template": {
	            "data": [
	                {
	                    "name": "username",
	                    "value": null,
	                    "prompt": "Username",
	                    "options": []
	                },
	                {
	                    "name": "name",
	                    "value": null,
	                    "prompt": "Name",
	                    "options": []
	                },
	                {
	                    "name": "email",
	                    "value": null,
	                    "prompt": "Email",
	                    "options": []
	                },
	                {
	                    "name": "phoneNumber",
	                    "value": null,
	                    "prompt": "Phone number",
	                    "options": []
	                },
	                {
	                    "name": "job",
	                    "value": null,
	                    "prompt": "Job",
	                    "options": [
	                        {
	                            "text": "Administrator",
	                            "value": "administrator"
	                        },
	                        {
	                            "text": "Collector",
	                            "value": "collector"
	                        },
	                        {
	                            "text": "Management",
	                            "value": "management"
	                        }
	                    ]
	                }
	            ]
	        }
	    }
	}
	
### Funcionário especifico
	GET /employees/:username
	
#### Variáveis
| Nome | Tipo | Descrição |
| --- | --- | --- |	
username | string | Nome de utilizador do funcionário a procurar

#### Resposta
	Status: 200 OK
	Content-Type: application/vnd.siren+json
	{
	    "class": [
	        "employee"
	    ],
	    "properties": {
	        "username": "admin",
	        "name": "Administrator",
	        "email": "therealadmin@wastecollecter.com",
	        "job": "administrator",
	        "phoneNumber": 912388798
	    },
	    "entities": [],
	    "actions": [
	        {
	            "name": "update-password",
	            "title": null,
	            "method": "PUT",
	            "href": "/employees/admin/credentials",
	            "type": "*/*",
	            "fields": []
	        },
	        {
	            "name": "change-employee-job",
	            "title": null,
	            "method": "PUT",
	            "href": "/employees/admin/job",
	            "type": "application/json",
	            "fields": [
	                {
	                    "name": "job",
	                    "type": "number",
	                    "value": null,
	                    "title": "Job",
	                    "options": [
	                        {
	                            "text": "Administrator",
	                            "value": "administrator"
	                        },
	                        {
	                            "text": "Collector",
	                            "value": "collector"
	                        },
	                        {
	                            "text": "Management",
	                            "value": "management"
	                        }
	                    ],
	                    "field_class": null
	                }
	            ]
	        },
	        {
	            "name": "delete-employee",
	            "title": null,
	            "method": "PUT",
	            "href": "/employees/admin",
	            "type": "*/*",
	            "fields": []
	        }
	    ],
	    "links": [
	        {
	            "rel": [
	                "self"
	            ],
	            "href": "/employees/admin"
	        },
	        {
	            "rel": [
	                "up"
	            ],
	            "href": "/employees"
	        }
	    ]
	}
	
### Funcionário autenticado
	GET /employees/current
Obter o perfil do funcionário que estiver autenticado no ato do pedido.
	
#### Resposta
	Status: 200 OK
	Content-Type: application/vnd.siren+json	{
	    "class": [
	        "employee"
	    ],
	    "properties": {
	        "username": "admin",
	        "name": "Administrator",
	        "email": "therealadmin@wastecollecter.com",
	        "job": "administrator",
	        "phoneNumber": 912388798
	    },
	    "entities": [],
	    "actions": [
	        {
	            "name": "update-password",
	            "title": null,
	            "method": "PUT",
	            "href": "/employees/admin/credentials",
	            "type": "*/*",
	            "fields": []
	        },
	        {
	            "name": "change-employee-job",
	            "title": null,
	            "method": "PUT",
	            "href": "/employees/admin/job",
	            "type": "application/json",
	            "fields": [
	                {
	                    "name": "job",
	                    "type": "number",
	                    "value": null,
	                    "title": "Job",
	                    "options": [
	                        {
	                            "text": "Administrator",
	                            "value": "administrator"
	                        },
	                        {
	                            "text": "Collector",
	                            "value": "collector"
	                        },
	                        {
	                            "text": "Management",
	                            "value": "management"
	                        }
	                    ],
	                    "field_class": null
	                }
	            ]
	        },
	        {
	            "name": "delete-employee",
	            "title": null,
	            "method": "PUT",
	            "href": "/employees/admin",
	            "type": "*/*",
	            "fields": []
	        }
	    ],
	    "links": [
	        {
	            "rel": [
	                "self"
	            ],
	            "href": "/employees/admin"
	        },
	        {
	            "rel": [
	                "up"
	            ],
	            "href": "/employees"
	        }
	    ]
	}
	
#### Classes
Nome | Descrição |
--- | --- |
employee | Identifica um Funcionário
	
#### Ações
Nome | Descrição |
--- | --- |
update-password | Atualiza a palavra passe associada à conta do funcionário
change-employee-job | Atualiza o cargo associado ao funcionário
delete-employee  | Apaga o funcionário e a sua conta do sistema

### Vocabulário
Nome | Tipo |Descrição | Restrições
--- | --- | --- | --- |
username | string | Nome de utilizador associado à conta do funcionário | -
name | string | Nome do funcionário | -
email | string | Email do funcionário | Deve seguir o padrão `[a..Z]@[a..Z].[a..Z]`
job | string | Cargo do funcionário | *job* = {*administrator, management, collector*}
phoneNumber | int | Número de telefone do funcionário | LENGTH(*phoneNumber*) = 9; *phoneNumber* começa com 91, 92, 93 ou 96
	

## <a name="references"></a> Referências
<a name="json-home-specification"></a> [1] [Especificação do Json Home](http://), visitado a 20/06/2018

<a name="collection-json-specification"></a> [2] [Especificação do Collection+Json](http://), visitado a 20/06/2018

<a name="siren-json-specification"></a> [3] [Especificação do Siren+Json](http://), visitado a 20/06/2018

<a name="problem-json-specification"></a> [3] [Especificação do Problem+Json](http://), visitado a 22/06/2018