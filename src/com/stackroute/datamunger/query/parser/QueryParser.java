package com.stackroute.datamunger.query.parser;

import java.util.ArrayList;
import java.util.List;

/*There are total 4 DataMungerTest file:
 * 
 * 1)DataMungerTestTask1.java file is for testing following 4 methods
 * a)getBaseQuery()  b)getFileName()  c)getOrderByClause()  d)getGroupByFields()
 * 
 * Once you implement the above 4 methods,run DataMungerTestTask1.java
 * 
 * 2)DataMungerTestTask2.java file is for testing following 2 methods
 * a)getFields() b) getAggregateFunctions()
 * 
 * Once you implement the above 2 methods,run DataMungerTestTask2.java
 * 
 * 3)DataMungerTestTask3.java file is for testing following 2 methods
 * a)getRestrictions()  b)getLogicalOperators()
 * 
 * Once you implement the above 2 methods,run DataMungerTestTask3.java
 * 
 * Once you implement all the methods run DataMungerTest.java.This test case consist of all
 * the test cases together.
 */

public class QueryParser {

	private QueryParameter queryParameter = new QueryParameter();

	/*
	 * This method will parse the queryString and will return the object of
	 * QueryParameter class
	 */
	public QueryParameter parseQuery(String queryString) {
		queryParameter.setFileName(findFileName(queryString));
		queryParameter.setBaseQuery(findBaseQuery(queryString));
		queryParameter.setOrderByFields(findOrderByFields(queryString));
		queryParameter.setGroupByFields(findGroupByFields(queryString));
		queryParameter.setFields(findFields(queryString));
		queryParameter.setLogicalOperators(findLogicalOperators(queryString));
		queryParameter.setAggregateFunctions(findAggregateFunctions(queryString));
		queryParameter.setRestrictions(findRestrictions(queryString));
		return queryParameter;
	}

	/*
	 * Extract the name of the file from the query. File name can be found after the
	 * "from" clause.
	 */
	public String findFileName(String queryString) {
		queryString = queryString.toLowerCase();
		int indexOfFromClause = queryString.indexOf("from");
		int indexOfCsv = queryString.indexOf("csv");
		return queryString.substring((indexOfFromClause+5), (indexOfCsv+3));
		
		
	}
	/*
	 * 
	 * Extract the baseQuery from the query.This method is used to extract the
	 * baseQuery from the query string. BaseQuery contains from the beginning of the
	 * query till the where clause
	 */
	
	public String findBaseQuery(String queryString) {
		queryString = queryString.toLowerCase();
		if(queryString.contains("where")) {
			return queryString.substring(0, (queryString.indexOf("where")-1));
		}else {
			if(queryString.contains("group by") && queryString.contains("order by")) {
				return queryString.substring(0, (queryString.indexOf("group by")-1));
			}else if(queryString.contains("group by")) {
				return queryString.substring(0, (queryString.indexOf("group by")-1));
			}else if(queryString.contains("order by")) {
				return queryString.substring(0, (queryString.indexOf("order by")-1));
			}else {
				return queryString;
			}
		}
	}
	

	/*
	 * extract the order by fields from the query string. Please note that we will
	 * need to extract the field(s) after "order by" clause in the query, if at all
	 * the order by clause exists. For eg: select city,winner,team1,team2 from
	 * data/ipl.csv order by city from the query mentioned above, we need to extract
	 * "city". Please note that we can have more than one order by fields.
	 */
	public List<String> findGroupByFields(String queryString) {
		queryString = queryString.toLowerCase();
		List<String> resList = new ArrayList<String>();
		if(queryString.contains("group by")) {
			String tmpArr = queryString.split("group by ")[1];
			if(tmpArr.indexOf("order by") > 0) {
				tmpArr = tmpArr.split("order by")[0];
			}
			String [] res =tmpArr.split(" ");
			for (int i = 0; i < res.length; i++) {
				resList.add(res[i]);
			}
			return resList;
		}

		return null;
	}
	/*
	 * Extract the group by fields from the query string. Please note that we will
	 * need to extract the field(s) after "group by" clause in the query, if at all
	 * the group by clause exists. For eg: select city,max(win_by_runs) from
	 * data/ipl.csv group by city from the query mentioned above, we need to extract
	 * "city". Please note that we can have more than one group by fields.
	 */
	public List<String> findOrderByFields(String queryString) {
		queryString = queryString.toLowerCase();
		List<String> resList = new ArrayList<String>();
		if(queryString.contains("order by")) {
			String tmpArr = queryString.split("order by ")[1];
			String [] res =tmpArr.split(" ");
			for (int i = 0; i < res.length; i++) {
				resList.add(res[i]);
			}
			return resList;
		}

		return null;
	}
	/*
	 * Extract the selected fields from the query string. Please note that we will
	 * need to extract the field(s) after "select" clause followed by a space from
	 * the query string. For eg: select city,win_by_runs from data/ipl.csv from the
	 * query mentioned above, we need to extract "city" and "win_by_runs". Please
	 * note that we might have a field containing name "from_date" or "from_hrs".
	 * Hence, consider this while parsing.
	 */
	public List<String> findFields(String queryString) {
		String [] str =queryString.substring((queryString.indexOf("select ")+7), (queryString.indexOf(" from"))).split(",");
		List <String> resList = new ArrayList<String>();
		for (int i = 0; i < str.length; i++) {
			resList.add(str[i]);
		}
		return resList;
	}
	/*
	 * Extract the conditions from the query string(if exists). for each condition,
	 * we need to capture the following: 1. Name of field 2. condition 3. value
	 * 
	 * For eg: select city,winner,team1,team2,player_of_match from data/ipl.csv
	 * where season >= 2008 or toss_decision != bat
	 * 
	 * here, for the first condition, "season>=2008" we need to capture: 1. Name of
	 * field: season 2. condition: >= 3. value: 2008
	 * 
	 * the query might contain multiple conditions separated by OR/AND operators.
	 * Please consider this while parsing the conditions.
	 * 
	 */
	public List<Restriction> findRestrictions(String queryString) {
		List<Restriction> conditions = null;
		String[] whereQuery;
		String tempString;
		String[] conditionQuery;
		String[] getCondition = null;
		if (queryString.contains("where")) {
			conditions = new ArrayList<Restriction>();
			whereQuery = queryString.trim().split("where ");
			if (whereQuery[1].contains("group by")) {
				conditionQuery = whereQuery[1].trim().split("group by");
				tempString = conditionQuery[0];
			} else if (whereQuery[1].contains("order by")) {
				conditionQuery = whereQuery[1].trim().split("order by");
				tempString = conditionQuery[0];
			} else {
				tempString = whereQuery[1];
			}
			getCondition = tempString.trim().split(" and | or ");
			String[] condSplit = null;
			if (getCondition != null) {
				for (int i = 0; i < getCondition.length; i++) {
					if (getCondition[i].contains("=")) {
						condSplit = getCondition[i].trim().split("\\W+");
						conditions.add(new Restriction(condSplit[0], condSplit[1], "="));
					} else if (getCondition[i].contains(">")) {
						condSplit = getCondition[i].trim().split("\\W+");
						conditions.add(new Restriction(condSplit[0], condSplit[1], ">"));
					} else if (getCondition[i].contains("<")) {
						condSplit = getCondition[i].trim().split("\\W+");
						conditions.add(new Restriction(condSplit[0], condSplit[1], "<"));
					}

				}
			}
		}
		return conditions;

	}

	/*
	 * Extract the logical operators(AND/OR) from the query, if at all it is
	 * present. For eg: select city,winner,team1,team2,player_of_match from
	 * data/ipl.csv where season >= 2008 or toss_decision != bat and city =
	 * bangalore
	 * 
	 * The query mentioned above in the example should return a List of Strings
	 * containing [or,and]
	 */
	public List<String> findLogicalOperators(String queryString) {
		queryString = queryString.toLowerCase();
		String tmpStr = null;
		if(queryString.contains("where")) {
			tmpStr = queryString.split("where ")[1];
			if(tmpStr.contains("group by")) {
				tmpStr = tmpStr.split("group by")[0];
			}else if(tmpStr.contains("order by")) {
				tmpStr = tmpStr.split("order by")[0];
			}
			String [] tmpArr = tmpStr.split(" ");
			List <String> resList = new ArrayList<>(); 
			for(int i =0; i<tmpArr.length;i++) {
				if(tmpArr[i].equals("and") || tmpArr[i].equals("or")) {
					resList.add(tmpArr[i]);
				}
			}
			return resList;
			}else {
				
				return null;
			}
	}
	/*
	 * Extract the aggregate functions from the query. The presence of the aggregate
	 * functions can determined if we have either "min" or "max" or "sum" or "count"
	 * or "avg" followed by opening braces"(" after "select" clause in the query
	 * string. in case it is present, then we will have to extract the same. For
	 * each aggregate functions, we need to know the following: 1. type of aggregate
	 * function(min/max/count/sum/avg) 2. field on which the aggregate function is
	 * being applied.
	 * 
	 * Please note that more than one aggregate function can be present in a query.
	 * 
	 * 
	 */
	public List<AggregateFunction> findAggregateFunctions(String queryString) {
		queryString = queryString.toLowerCase();
		String val = queryString.split("from")[0];
		String tmpVal = val.split(" ")[1];
		String [] arr = tmpVal.split(",");
		List <AggregateFunction> myList = new ArrayList<AggregateFunction>();
		for (int i = 0; i < arr.length; i++) {
			if(arr[i].contains("count")) {
				myList.add(new AggregateFunction(arr[i].substring(6,arr[i].length()-1), arr[i].substring(0,5)));
			}else if(arr[i].contains("min") || arr[i].contains("max")
					|| arr[i].contains("avg") || arr[i].contains("sum")){
				myList.add(new AggregateFunction(arr[i].substring(4,arr[i].length()-1), arr[i].substring(0,3)));
			}
		}
		return myList;
	}

}