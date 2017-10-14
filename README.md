# external-api

External api is the server for iRobo's official web page. 

## 1. Getting Started

1. Make "application.conf" from "application.conf.sample" and fill in database configurations.

2. For testing, run "test" in sbt mode.

3. For running main, run "run" in sbt mode.

4. After work is complete, run "assembly" in sbt mode for deployment jar file.

## 2. Architecture

Portfolio engine has three major components in its architecture : Model, Service, Database Access Object (DAO), Endpoint 

### 1) Model

Each model defines major components of the page such as user and portfolio.

- Session
- DongbuUser
- Portfolio

### 2) Service

Service is used to load data from database, process each row into various market data, and compute ranks of five different styles (growth, growth value, equity value, dividend value, large).

- 

### 3) Database Access Object (DAO)

DAO contains queries to the database.

- CriteriaDao
- CustomerDao

### 4) Endpoint

Endpoint defines how services are served under a particular url.

- CustomerEndpoint
- PortfolioEndpoint
- DongbuUserEndpoint
- AuthEndpoint

## 3. Model

### 1) Session

``` Scala
/** Session
  * name          : John Smith
  * email         : johnsmith@gmail.com
  * cellphone     : 01011111111
  * age           : 20
  * tendency      : 1 (aggressive) or 0 (conservative)
  * lossTolerance : 0.20 (loss tolerance of up to 20%)
  * rank          : 1~5
  * score         : 0 ~ 100
  * preferredRank : rank that user wishes to update to
  * uuid          : uuid of the user that is used to track which user wishes to update rank.
  */

case class Session(
  id            : Int,
  createdAt     : DateTime,
  name          : String,
  email         : String,
  cellphone     : String,
  age           : Int,
  tendency      : Int,
  lossTolerance : Double,
  retired       : Boolean,
  rank          : Int,
  score         : Int,
  preferredRank : Int,
  uuid          : String
)
```

Session is a representation of user. It contains user's information such as name, email, phone number, etc.

### 2) Dongbu User Question

```Scala
/** Dongbu User Question (user's answer choices for each question)	
  * ageRange         : 1 ~ 6
  * monthlyIncome    : 1 ~ 5 
  * incomeSource     : 1 ~ 3 
  * pureAsset        : 1 ~ 5
  * finAssetRatio    : 1 ~ 5 
  * productAware     : [1 ~ 5] // multiple choice
  * investUnderstand : 1 ~ 5
  * riskWilling      : 1 ~ 4
  * investAim        : 1 ~ 3
  * investPeriod     : 1 ~ 5
  */

case class DongbuUserQuestion(
  ageRange         : Int,
  monthlyIncome    : Int,
  incomeSource     : Int,
  pureAsset        : Int,
  finAssetRatio    : Int,
  productAware     : Array[Int],
  investUnderstand : Int,
  riskWilling      : Int,
  investAim        : Int,
  investPeriod     : Int
)
```

Session responses contains user's answer picks to each question of iRobo's questionnaire. 

### 3) Dongbu User Info

```Scala
/** Dongbu User Info
  * age           : 20
  * tendency      : 1 (aggressive) or 0 (conservative)
  * retired       : true (retired) or false(active)
  * lossTolerance : 0.20 (loss tolerance of up to 20%)
  */
  
case class DongbuUserInfo(
  age           : Int, 
  lossTolerance : Double, 
  tendency      : Boolean, 
  retired       : Boolean
)
```

Represents dongbu user's basic information (age, loss tolerance, tendency, retired)

### Style Aggressiveness

``` Scala
 /** Style Aggressiveness
   * 
   * style          : rank from 1 (aggressive) ~ 6 (conservative) that defines user's investment aggressiveness
   * aggressiveness : one line description of user's style (1: "공격투자형", 2: "적극투자형", 3: "투자형", 4: "중립형", 5: "안정추구형", 6: "안정형")
   */
 
 case class StyleAggressiveness(style: Int, aggressiveness: String)
```

Represents user's invest style and description of his or her aggressiveness.

### 4) Dongbu Portfolio 

```Scala
/** Dongbu Portfolio
  * stockRatio : eg. 0.20
  * bondRatio  : eg. 0.80
  */
  
case class DongbuPortfolio(
  stockRatio : StockRatio,
  bondRatio  : BondRatio
)
```

### 5) Dongbu Latter Question 

```Scala
/** Dongbu Latter Question
  * retired index        : 1 (yes), 2(no) 
  * tendency index       : 1 (aggressive), 2 (conservative)
  * loss tolerance index : 1(0%), 2(0~5%), 3(5~10%), 4(10~15%), 5(15~20%), 6(20~25%), 7(25~30%), 8(30%~), 
  */
  
case class DongbuLatterQuestion(
  retiredIndex       : Int,
  tendencyIndex      : Int,
  lossToleranceIndex : Int
)
```

Represents user's picks for question 11, 12, 13. This is automatically filled by user's age, status, tendency suggested by user's input for personal information.

### 4) Dongbu User Result Data

```Scala
/** Dongbu User Result Data
  * sessionUudid : 1 (yes), 2(no) 
  * rank         : 등급 1 (aggressive) ~ 6 (conservative) 
  * score        : user's total score from questionnaire
  * riskNumber   : 위험도 (0 ~ 5.0)
  * intro        : summary of user
  * plan         : iRobo's suggestion to the user
  * mobileText   : intro and plan text optimized for mobile rendering
  */

case class DongbuUserResultData(
  sessionUuid          : String,
  rank                 : Int,
  score                : Int,
  riskNumber           : Double,
  intro                : String,
  plan                 : String,
  mobileText           : String,
  styleAggressiveness  : StyleAggressiveness,
  dongbuPortfolio      : DongbuPortfolio,
  dongbuLatterQuestion : DongbuLatterQuestion
)
```

## Service

### 1. PortfolioService

```Scala
 /** Input
   * age           : user's age
   * lossTolerance : user's loss tolerance (eg. 0.20)
   * tendency      : user's aggressiveness (true: aggeressive, false: conservative)
   * retired       : user's retired state (true: retired, false: active)
   */
 def getPortfolioComposition(age: Int, lossTolerance: Double, tendency: Boolean, retired: Boolean)
```

```Scala
 /** Output
   *
   * stockBondRatio : stock to bond ratio
   * riskRatio      : user's aggressiveness on a scale of 1.0 ~ 5.0
   * style          : user's investment style 
   * 
   */
   
 val portfolioComposition = new PortfolioContent(
   stockBondRatio,
   riskRatio,
   style,
   intro,
   plan,
   title
 )
```

Gets user's age, loss tolerance, tendency, and status to calculate style, risk property ratio, stock to bond ratio, and  

### 2. DongbuUserService

```Scala
 /** Input
   * dongbuUserQuestion : user's questionnaire output
   * dongbuUserInfo     : user's information on age, loss tolerance, retirement, and tendency
   * sessionUuid        : uuid used to track user in case he or she wishes to update investment aggressiveness rank.
   */
 def getUserRankTendency(dongbuUserQuestion: DongbuUserQuestion, dongbuUserInfo: DongbuUserInfo, sessionUuid: String)
```

```Scala
 /** Output
   *
   * stockBondRatio : stock to bond ratio
   * riskRatio      : user's aggressiveness on a scale of 1.0 ~ 5.0
   * style          : user's investment style 
   * 
   */
   
 val portfolioComposition = new PortfolioContent(
   stockBondRatio,
   riskRatio,
   style,
   intro,
   plan,
   title
 )
```
