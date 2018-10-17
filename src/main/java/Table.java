
public class Table {
	private String tableName;
	private String colName;
	private int colIndex;
	private String colDatatype;
	private String colLength;
	private String isPrimaryKey;
	private String isPartitioned;
	private String defaultValue;
	
	public String getTableName() {
		return tableName;
	}
	public void setTableName(String tableName) {
		this.tableName = tableName;
	}
	public String getColName() {
		return colName;
	}
	public void setColName(String colName) {
		this.colName = colName;
	}
	public int getColIndex() {
		return colIndex;
	}
	public void setColIndex(int colIndex) {
		this.colIndex = colIndex;
	}
	public String getColDatatype() {
		return colDatatype;
	}
	public void setColDatatype(String colDatatype) {
		this.colDatatype = colDatatype;
	}
	public String getColLength() {
		return colLength;
	}
	public void setColLength(String colLength) {
		this.colLength = colLength;
	}
	
	public String getIsPrimaryKey() {
		return isPrimaryKey;
	}
	public void setIsPrimaryKey(String isPrimaryKey) {
		this.isPrimaryKey = isPrimaryKey;
	}
	public String getIsPartitioned() {
		return isPartitioned;
	}
	public void setIsPartitioned(String isPartitioned) {
		this.isPartitioned = isPartitioned;
	}
	public String getDefaultValue() {
		return defaultValue;
	}
	public void setDefaultValue(String defaultValue) {
		this.defaultValue = defaultValue;
	}
	@Override
	public String toString() {
		return "HiveTable [tableName=" + tableName + ", colName=" + colName + ", colIndex=" + colIndex
				+ ", colDatatype=" + colDatatype + ", colLength=" + colLength + ", isPrimaryKey=" + isPrimaryKey
				+ ", isPartitioned=" + isPartitioned + ", defaultValue=" + defaultValue + "]";
	}
	
	
	

}
