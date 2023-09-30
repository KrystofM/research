
the_color = "papayawhip"
data = read.csv(file.choose(), header=TRUE)
drops = c("Year.and.survey", "Inequality.among.the.poor")
data = data[ , !(names(data) %in% drops)]
data$Number.of.poor..2017. = strtoi(gsub(",", "",data$Number.of.poor..2017.))
#library for filtering
library(dplyr)
regions = c("Arab States", "East Asia and the Pacific", "Europe and Central Asia",
            "Latin America and the Caribbean","South Asia","Sub-Saharan Africa")
#filter out regions
data = filter(data, !(Country %in% regions))
data = na.omit(data)
attach(data)

#Section 3
poor_population_log = log(Number.of.poor..2017., base = 10)
qqnorm(poor_population_log, main = "Normal QQ Plot: Logarithmic Number of Poor (2017)", pch=0)
qqline(poor_population_log, col="red")
shapiro.test(poor_population_log)

#Histogram of the number of poor vs the Density.
poor_population_log = log(Number.of.poor..2017., base = 10)
hist(poor_population_log, main="Histogram: Logarithmic Number of Poor (2017)", prob=TRUE,
                      col = the_color, breaks = 6, xlab="Number of Poor (Logarithmic)")
#add a normal p.d.f.
m=mean(poor_population_log, na.rm=T)
stdev=sd(poor_population_log, na.rm=T)
curve(dnorm(x,mean=m, sd=stdev),col="red",add=TRUE)
first_digit = as.numeric(substr(Number.of.poor..2017., 1, 1));
hist(first_digit, main="Histogram: First digit of Number of Poor", prob=TRUE, col = the_color,
                                        breaks = 6, xlab="First digit of Number of Poor")

#Section 4.1
outlier_percentile = .1
data$Small.country = data$Number.of.poor..2017. < quantile(data$Number.of.poor..2017.,
                                                           2*outlier_percentile)
data$Outlier = data$Index < quantile(data$Index, outlier_percentile) | 
              data$Index > quantile(data$Index, 1-outlier_percentile)
contingency_table = contingency_table = table(data$Outlier, data$Small.country,
                                              dnn = c("Outlier", "Small"))
contingency_table
fisher.test(contingency_table,alternative = "greater")

# Section 4.2
# Compute the difference between National poverty line and International poverty line
data$Difference = data$National.poverty.line - data$PPP..1.90.a.day
# Compute the mean of the difference
mean_difference = mean(data$Difference)
# Compute the standard deviation of the difference
sd_difference = sd(data$Difference)
# Hypothesis test
# H0: mean_difference = 0
# H1: mean_difference > 0
# alpha = 0.05
# t-statistic
t_statistic = mean_difference / (sd_difference / sqrt(length(data$Difference)))
# quantile of the t-distribution
quantile_t = qt(0.99, df = length(data$Difference) - 1)
# Conclusion
if (quantile_t < t_statistic) {
  print("Reject H0")
} else {
  print("Accept H0")
}

#Section 4.3: Poverty in small vs big countries
#calculate population
data$population = 1000*data$Number.of.poor..2017. / (data$National.poverty.line/100)
med = median(data$population)
#data set for small and big countries
small = filter(data, data$population <= med)
big = filter(data, data$population > med)
#calculate number of people in small countries and number of people in big countries
n = sum(small$population)
m = sum(big$population)
#sample proportion of small and big countries
sp1 = 1000*sum(small$Number.of.poor..2017.) / n
sp2 = 1000*sum(big$Number.of.poor..2017.) / m

#statistic and p_value
Z = (sp1 - sp2) / sqrt((sp1*(1 - sp1)/n + sp2*(1 - sp2)/m))
p_value = 2*(1 - pnorm(Z))

#Section 5
#building a multi-linear regression model using Index as response variable 
#and the other factors as explanatory variables. 
#Result shows that only Headcount and Population in severe multidimensional poverty are relevant.
model <- lm(Index ~ Headcount + Intensity.of.deprivation + Number.of.poor..2017. +
              Population.in.severe.multidimensional.poverty + 
              Population.vulnerable.to.multidimensional.poverty + Health + 
              Education + Standard.of.living , data = data)
summary(model)

#Building a new regression model
regr <- lm(Index~Headcount + Population.in.severe.multidimensional.poverty, data=data)
summary(regr)

#plot the residuals
plot(Index, residuals(regr),col="blue",pch=4)
abline(regr)

############ PREDICTION INTERVAL ############
#compute a 95% prediction interval for Index 
#given that Headcount = 100 and 
#Population.in.severe.multidimensional.poverty = 15.
newdata=data.frame(Headcount = 100, Population.in.severe.multidimensional.poverty =15)
predict(regr, newdata, interval="predict")

#fit       lwr       upr
#1 0.3753793 0.3645504 0.3862082

detach(data)

