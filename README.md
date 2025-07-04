
# income-tax-employment
This is where we make API calls from users viewing and making changes to the employment section of their income tax return.

## Running the service locally

You will need to have the following:
- Installed/configured [service manager](https://github.com/hmrc/service-manager).

The service manager profile for this service is:

    sm2 --start INCOME_TAX_EMPLOYMENT
Run the following command to start the remaining services locally:

    sudo mongod (If not already running)
    sm2 --start INCOME_TAX_SUBMISSION_ALL -r

This service runs on port: `localhost:9315`

## Development

To build and test the project, run:

```
sbt clean compile test it/test
```
To run code coverage

```
sbt clean coverage test it/test coverageReport
```

### Employment endpoints:

- **GET     /income-tax/nino/:nino/sources/:employmentId**  (Gets employment data for particular employment id and source)          

- **GET     /income-tax/nino/:nino/sources** (Gets all employment data for a Nino, this calls many APIs to fetch and collate all the data)    
                         
- **DELETE  /income-tax/nino/:nino/sources/:employmentId/unignore** (Reinstates a particular employment)

- **DELETE  /income-tax/nino/:nino/sources/:employmentId/:toRemove** (Removes a particular employment, this could result in numerous API calls to remove / ignore the relevant employment data)

- **POST    /income-tax/nino/:nino/sources** (Creates or updates an employment based on the request body, this can also ignore hmrc data if an update is being made to employment details such as a change of name or employer reference etc.)                            

### Connected microservices
We connect to two other microservices to fetch some data. These two services will ultimately call one of the two above downstream services for their data. 
- income-tax-expenses (Gets employment expenses data)

### Employment Sources (HMRC-Held and Customer Data)
Employment data can come from different sources: HMRC-Held and Customer. HMRC-Held data is employment data that HMRC have for the user within the tax year, prior to any updates made by the user. The employment data displayed in-year is HMRC-Held.

Customer data is provided by the user. At the end of the tax year, users can view any existing employment data and make changes (create, update and delete).

<details>
<summary>Click here to see an example of a user with HMRC-Held and Customer data (JSON)</summary>

```json
{
  "employment": [
    {
      "taxYear": 2022,
      "hmrcEmployments": [
        {
          "employmentId": "00000000-0000-1000-8000-000000000000",
          "employerName": "Vera Lynn",
          "employerRef": "123/12345",
          "payrollId": "123345657",
          "startDate": "2020-06-17",
          "cessationDate": "2020-06-17",
          "dateIgnored": "2020-06-17T10:53:38Z",
          "employmentData": {
            "submittedOn": "2020-01-04T05:01:01Z",
            "source": "HMRC-HELD",
            "employment": {
              "employmentSequenceNumber": "1002",
              "payrollId": "123456789999",
              "companyDirector": false,
              "closeCompany": true,
              "directorshipCeasedDate": "2020-02-12",
              "startDate": "2019-04-21",
              "cessationDate": "2020-03-11",
              "occPen": false,
              "disguisedRemuneration": false,
              "employer": {
                "employerRef": "223/AB12399",
                "employerName": "maggie"
              },
              "pay": {
                "taxablePayToDate": 34234.15,
                "totalTaxToDate": 6782.92,
                "payFrequency": "CALENDAR MONTHLY",
                "paymentDate": "2020-04-23",
                "taxWeekNo": 32
              },
              "deductions": {
                "studentLoans": {
                  "uglDeductionAmount": 13343.45,
                  "pglDeductionAmount": 24242.56
                }
              },
              "benefitsInKind": {
                "accommodation": 100,
                "assets": 100,
                "assetTransfer": 100,
                "beneficialLoan": 100,
                "car": 100,
                "carFuel": 100,
                "educationalServices": 100,
                "entertaining": 100,
                "expenses": 100,
                "medicalInsurance": 100,
                "telephone": 100,
                "service": 100,
                "taxableExpenses": 100,
                "van": 100,
                "vanFuel": 100,
                "mileage": 100,
                "nonQualifyingRelocationExpenses": 100,
                "nurseryPlaces": 100,
                "otherItems": 100,
                "paymentsOnEmployeesBehalf": 100,
                "personalIncidentalExpenses": 100,
                "qualifyingRelocationExpenses": 100,
                "employerProvidedProfessionalSubscriptions": 100,
                "employerProvidedServices": 100,
                "incomeTaxPaidByDirector": 100,
                "travelAndSubsistence": 100,
                "vouchersAndCreditCards": 100,
                "nonCash": 100
              }
            }
          }
        }
      ],
      "customerEmployments": [
        {
          "employmentId": "00000000-0000-1000-8000-000000000002",
          "employerName": "Vera Lynn",
          "employerRef": "123/12345",
          "payrollId": "123345657",
          "startDate": "2020-06-17",
          "cessationDate": "2020-06-17",
          "submittedOn": "2020-06-17T10:53:38Z",
          "employmentData": {
            "submittedOn": "2020-02-04T05:01:01Z",
            "employment": {
              "employmentSequenceNumber": "1002",
              "payrollId": "123456789999",
              "companyDirector": false,
              "closeCompany": true,
              "directorshipCeasedDate": "2020-02-12",
              "startDate": "2019-04-21",
              "cessationDate": "2020-03-11",
              "occPen": false,
              "disguisedRemuneration": false,
              "employer": {
                "employerRef": "223/AB12399",
                "employerName": "maggie"
              },
              "pay": {
                "taxablePayToDate": 34234.15,
                "totalTaxToDate": 6782.92,
                "payFrequency": "CALENDAR MONTHLY",
                "paymentDate": "2020-04-23",
                "taxWeekNo": 32
              },
              "deductions": {
                "studentLoans": {
                  "uglDeductionAmount": 13343.45,
                  "pglDeductionAmount": 24242.56
                }
              },
              "benefitsInKind": {
                "accommodation": 100,
                "assets": 100,
                "assetTransfer": 100,
                "beneficialLoan": 100,
                "car": 100,
                "carFuel": 100,
                "educationalServices": 100,
                "entertaining": 100,
                "expenses": 100,
                "medicalInsurance": 100,
                "telephone": 100,
                "service": 100,
                "taxableExpenses": 100,
                "van": 100,
                "vanFuel": 100,
                "mileage": 100,
                "nonQualifyingRelocationExpenses": 100,
                "nurseryPlaces": 100,
                "otherItems": 100,
                "paymentsOnEmployeesBehalf": 100,
                "personalIncidentalExpenses": 100,
                "qualifyingRelocationExpenses": 100,
                "employerProvidedProfessionalSubscriptions": 100,
                "employerProvidedServices": 100,
                "incomeTaxPaidByDirector": 100,
                "travelAndSubsistence": 100,
                "vouchersAndCreditCards": 100,
                "nonCash": 100
              }
            }
          }
        }
      ],
      "employmentExpenses": {
        "submittedOn": "2022-12-12T12:12:12Z",
        "dateIgnored": "2022-12-11T12:12:12Z",
        "source": "HMRC-HELD",
        "totalExpenses": 100,
        "expenses": {
          "businessTravelCosts": 100,
          "jobExpenses": 100,
          "flatRateJobExpenses": 100,
          "professionalSubscriptions": 100,
          "hotelAndMealExpenses": 100,
          "otherAndCapitalAllowances": 100,
          "vehicleExpenses": 100,
          "mileageAllowanceRelief": 100
        }
      }
    }
  ]
}
```

</details>

## Ninos with stub data for employment

### In-Year
| Nino      | Employment data                                                  | Source    |
|-----------|------------------------------------------------------------------|-----------|
| AA123459A | Single employment - Employment details and expenses              | HMRC-Held |
| AA133742A | Single employment - Employment details, benefits and expenses    | HMRC-Held |
| BB444444A | Multiple employments - Employment details, benefits and expenses | HMRC-Held |
| AA370773A | Multiple employments - `occPen` set to true                      | HMRC-Held |

### End of Year
| Nino      | Employment data                                                  | Source              |
|-----------|------------------------------------------------------------------|---------------------|
| AA123459A | Single employment - Employment details and expenses              | HMRC-Held, Customer |
| AA133742A | Single employment - Employment details and benefits              | HMRC-Held, Customer |
| BB444444A | Multiple employments - Employment details, benefits and expenses | HMRC-Held, Customer |
| AA370773A | Multiple employments - `occPen` set to true                      | HMRC-Held, Customer |
| AA455555A | User with ignored hmrc data (Employments can be reinstated)      | HMRC-Held           |
| AA333444A | User with only expenses data                                     | HMRC-Held           |

### License

This code is open source software licensed under the [Apache 2.0 License]("http://www.apache.org/licenses/LICENSE-2.0.html").
