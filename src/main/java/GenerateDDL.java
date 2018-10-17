import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import com.opencsv.CSVReader;
import com.opencsv.bean.CsvToBean;
import com.opencsv.bean.HeaderColumnNameTranslateMappingStrategy;

public class GenerateDDL {
	private static String txtFileName = "";
	private static String hiveTableDelimiter = ",";
	private static String dbType = "";
	private static String hiveFileExtension = ".hql";
	private static String fileExtension = ".sql";
	private static String folderPath = "";
	
	public static void main(String[] args) 
    { 
		 if(args.length>0 && (args.length ==3 || args.length ==2)) {
			 dbType = args[0];
			 txtFileName = args[1];
			 if(args.length ==3)
				 folderPath = args[2];
			 CSVReader csvReader = null; 
		     try { 
	            csvReader = new CSVReader(new FileReader (txtFileName)); 
	            List<Table> tableList = readCSV(csvReader);
	            if(dbType.equalsIgnoreCase("Hive"))
	            	generateHiveTableDDl(tableList, hiveFileExtension);
	            if(dbType.equalsIgnoreCase("Oracle") || dbType.equalsIgnoreCase("Postgress") || dbType.equalsIgnoreCase("Mysql"))
		            	generateTableDDl(tableList, fileExtension);
		            
		      } catch (FileNotFoundException e) { 
		            // TODO Auto-generated catch block 
		            e.printStackTrace(); 
		      } 
	     } else {
	    	 System.out.println("Please enter dbType (hive/mysql/oracle/postgress) , CSV file , folderPath in sequence ");
	    	 System.exit(1);
	     }
		
    }

	private static List<Table> readCSV(CSVReader csvReader) {
		// Hashmap to map CSV data to  
        // Bean attributes. 
        Map<String, String> mapping = new HashMap<String, String>(); 
        
        mapping.put("TABLE", "tableName"); 
        mapping.put("INDEX", "colIndex"); 
        mapping.put("COLUMN", "colName"); 
        mapping.put("DATATYPE", "colDatatype"); 
        mapping.put("LENGTH", "colLength"); 
        mapping.put("PRIMARYKEY", "isPrimaryKey"); 
        mapping.put("PARTKEY", "isPartitioned"); 
        mapping.put("DEFAULT", "defaultValue"); 
  
        HeaderColumnNameTranslateMappingStrategy<Table> strategy = new HeaderColumnNameTranslateMappingStrategy<Table>(); 
        strategy.setType(Table.class); 
        strategy.setColumnMapping(mapping); 
        
        CsvToBean<Table> csvToBean = new CsvToBean<Table> (); 
  
        List<Table> list = csvToBean.parse(strategy, csvReader); 
        return list;
	} 
	
	public static String generateHiveTableDDl(List<Table> list, String fileExtension) {
		StringBuilder builder = null;
		Map<String, List<Table>> tableMap = populateMapFromList(list);
		
		for(String tableName : tableMap.keySet()) {
			builder = new StringBuilder();
			Map<String, List<Table>> partitionedColumnMap = new LinkedHashMap<String, List<Table>>();
			builder.append("DROP TABLE IF EXISTS " + tableName +";").append("\n");
			builder.append("CREATE TABLE " +  tableName + " (").append("\n");
			for(Table tableObj : tableMap.get(tableName)) {
				
				String colName = tableObj.getColName();
				String dataType = tableObj.getColDatatype();
				String isPartitionedColumn =  tableObj.getIsPartitioned();
				String colLength = tableObj.getColLength();
				if(isPartitionedColumn.equalsIgnoreCase("N")) {
					builder.append(colName + " " + dataType + " ("  + colLength + ")" + " ,").append("\n");
				} else {
					if(partitionedColumnMap.isEmpty()) {
						List<Table> partitionedList = new LinkedList<Table>();
						partitionedList.add(tableObj);
						partitionedColumnMap.put(tableName, partitionedList);
					} else {
						List<Table> l = partitionedColumnMap.get(tableName);
						l.add(tableObj);
					}
				}
			}
			builder.deleteCharAt(builder.length()-2);
			if(!partitionedColumnMap.values().isEmpty())
				builder.append(")").append("\n").append(" PARTITIONED BY ( ");
			for(String partitionedTableName : partitionedColumnMap.keySet()) {
				for(Table obj : partitionedColumnMap.get(partitionedTableName)) {
					builder.append(obj.getColName() + " " + obj.getColDatatype() + ",").append("\n");
				}
			}
			builder.deleteCharAt(builder.length()-2);
			builder.append(") ROW FORMAT DELIMITED FIELDS TERMINATED BY " + "'" + hiveTableDelimiter + "'" + " ;");
			builder.append("\n");
			writetoFile(builder, tableName, fileExtension);
		}
		return builder.toString();
	}
	
	public static Map<String, List<Table>> populateMapFromList(List<Table> list) {
		Map<String, List<Table>> tableMap = new LinkedHashMap<String, List<Table>>();
		
		Set<String> tableNameSet =  new LinkedHashSet<String>();
		for(Table table : list) {
			tableNameSet.add(table.getTableName());
		}
		
		for(String tableName : tableNameSet) {
			List<Table> l = new LinkedList<Table>();
			tableMap.put(tableName, l);
			for(Table table : list) {
				if(tableName.equalsIgnoreCase(table.getTableName())) {
					List<Table> tempList = tableMap.get(tableName);
					tempList.add(table);
					tableMap.put(tableName, tempList);
				}
			}
		}
		return tableMap;
	}
	
	public static String generateTableDDl(List<Table> list, String fileExtension) {
		StringBuilder builder = null;
		Map<String, List<Table>> tableMap = populateMapFromList(list);
		
		for(String tableName : tableMap.keySet()) {
			builder = new StringBuilder();
			Map<String, List<Table>> primaryColumnMap = new LinkedHashMap<String, List<Table>>();
			builder.append("DROP TABLE IF EXISTS " + tableName +";").append("\n");
			builder.append("CREATE TABLE " +  tableName + " (").append("\n");
			for(Table tableObj : tableMap.get(tableName)) {
				
				String colName = tableObj.getColName();
				String dataType = tableObj.getColDatatype();
				String isPrimaryColumn =  tableObj.getIsPrimaryKey();
				String colLength = tableObj.getColLength();
				String colDefault = (String) tableObj.getDefaultValue();
				if(isPrimaryColumn.equalsIgnoreCase("N")) {
					if(colDefault !="" && colDefault!=null && !colDefault.isEmpty())
						builder.append(colName + " " + dataType + " ("  + colLength + ")" + " DEFAULT " + colDefault + " ,").append("\n");
					else
						builder.append(colName + " " + dataType + " ("  + colLength + ")" + " ,").append("\n");
					
				} else {
					if(colDefault !="" && colDefault!=null && !colDefault.isEmpty())
						builder.append(colName + " " + dataType + " ("  + colLength + ")" + " NOT NULL" + " DEFAULT " + colDefault + " ,").append("\n");
					else
						builder.append(colName + " " + dataType + " ("  + colLength + ") " + " NOT NULL"+ " ,").append("\n");
					if(primaryColumnMap.isEmpty()) {
						List<Table> primaryKeyList = new LinkedList<Table>();
						primaryKeyList.add(tableObj);
						primaryColumnMap.put(tableName, primaryKeyList);
					} else {
						List<Table> l = primaryColumnMap.get(tableName);
						l.add(tableObj);
					}
				}
			}
			builder.deleteCharAt(builder.length()-2);
			if(!primaryColumnMap.values().isEmpty()) {
				builder.deleteCharAt(builder.length()-1).append(",\n").append("CONSTRAINT " + tableName + "_pk" +  " PRIMARY KEY (");
				for(String partitionedTableName : primaryColumnMap.keySet()) {
					for(Table obj : primaryColumnMap.get(partitionedTableName)) {
						builder.append(obj.getColName());
						builder.append(",");
					}
				}
				builder.deleteCharAt(builder.length()-1);
				builder.append(")").append("\n");
			}
			builder.append(")").append(";").append("\n");
			writetoFile(builder, tableName, fileExtension);
		}
		return builder.toString();
		
	}

	private static void writetoFile(StringBuilder builder, String tableName, String fileExtension) {
		if(folderPath!=null && folderPath!="")
			folderPath = folderPath + "/";
		String fileName = folderPath + tableName.toLowerCase() + fileExtension;
		BufferedWriter bwr = null;
		try {
			bwr = new BufferedWriter(new FileWriter(new File(fileName)));
		} catch (IOException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
		//write contents of StringBuffer to a file
		try {
			bwr.write(builder.toString());
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		//flush the stream
		try {
			bwr.flush();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		//close the stream
		try {
			bwr.close();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		System.out.println("Contents are written to File !!");
	}
} 

