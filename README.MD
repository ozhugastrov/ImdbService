# Movie Info Aggregator
This is simple application that aggregates movie title information from the https://imdbapi.dev/ platform.
It has single endpoint that takes `titleName` as a query parameter and returns movie information in JSON format.
### It has two caching layers:
1. cache entire response for a given titleName for configurable amount of time
2. cache individual movie information for a configurable amount of time

User can use `parentalRatingThreshold` query parameter to filter out movies where SEXUAL_CONTENT, VIOLENCE categories is reported as moderate or higher and 
severity is above the given threshold.

If `parentalRatingThreshold` is provided first cache layer is bypassed and only second cache layer is used.
Application has configurable retry mechanism to handle TOO_MANY_REQUESTS response from the imdbapi.dev platform.

## To run the application use:
```
sbt run
```
## Sample requests and example responses:
```
curl "http://localhost:9090?titleName=Inception"

{
    "type": "movie",
    "primaryTitle": "Inception",
    "genres":
    [
        "Action",
        "Adventure",
        "Sci-Fi",
        "Thriller"
    ],
    "rating":
    {
        "aggregateRating": 8.8,
        "voteCount": 2733854
    },
    "startYear": 2010,
    "directors":
    [
        {
            "id": "nm0634240",
            "displayName": "Christopher Nolan",
            "alternativeNames":
            [
                "Chris Nolan"
            ]
        }
    ],
    "writers":
    [
        {
            "id": "nm0634240",
            "displayName": "Christopher Nolan",
            "alternativeNames":
            [
                "Chris Nolan"
            ]
        }
    ],
    "actors":
    [
        {
            "id": "nm0000138",
            "displayName": "Leonardo DiCaprio",
            "alternativeNames":
            [
                "Leonardo Di Caprio",
                "Leo DiCaprio"
            ],
            "category": "actor"
        },
        {
            "id": "nm0182839",
            "displayName": "Marion Cotillard",
            "category": "actress"
        },
        {
            "id": "nm0330687",
            "displayName": "Joseph Gordon-Levitt",
            "alternativeNames":
            [
                "Joey Gordon-Levitt",
                "Joe Gordon-Levitt",
                "Joseph Gordon Levitt",
                "Joseph Leonard Levitt",
                "Joseph-Gordon Levitt",
                "RegularJOE"
            ],
            "category": "actor"
        },
        {
            "id": "nm3637500",
            "displayName": "Claire Geare",
            "alternativeNames":
            [
                "Claire Astin Geare"
            ],
            "category": "actress"
        },
        {
            "id": "nm0680983",
            "displayName": "Elliot Page",
            "category": "actor"
        }
    ],
    "cast":
    [
        {
            "id": "nm0634240",
            "displayName": "Christopher Nolan",
            "alternativeNames":
            [
                "Chris Nolan"
            ],
            "category": "writer"
        },
        {
            "id": "nm0634240",
            "displayName": "Christopher Nolan",
            "alternativeNames":
            [
                "Chris Nolan"
            ],
            "category": "director"
        },
        {
            "id": "nm2855622",
            "displayName": "Taylor Geare",
            "alternativeNames":
            [
                "Taylor Grace Geare"
            ],
            "category": "actress"
        },
        {
            "id": "nm0362766",
            "displayName": "Tom Hardy",
            "alternativeNames":
            [
                "Thomas Hardy"
            ],
            "category": "actor"
        },
        {
            "id": "nm3886541",
            "displayName": "Miranda Nolan",
            "category": "actress"
        }
    ]
}
```

Another example could be 
```
curl 'http://localhost:9090?titleName=Scrubs'

curl 'http://localhost:9090?titleName=The%20Lord%20of%20the%20Rings'

curl 'http://localhost:9090?titleName=The%20Lord%20of%20the%20Rings&parentalRatingThreshold=40'  
```



