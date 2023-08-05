package org.in.com.dao;

import java.sql.*;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

public class NamedPrepareStatement {

	private final PreparedStatement preparedStatement;

	private final Map<String, List<Integer>> indexMap;

	public NamedPrepareStatement(Connection connection, String query) throws SQLException {
		indexMap = new HashMap<>();
		String parsedQuery = parse(query);
		preparedStatement = connection.prepareStatement(parsedQuery);
	}

	private String parse(String query) {
		int length = query.length();
		StringBuilder stringBuilder = new StringBuilder(length);
		boolean inSingleQuote = false;
		boolean inDoubleQuote = false;
		int index = 1;
		for (int i = 0; i < length; i++) {
			char c = query.charAt(i);
			if (inSingleQuote) {
				if (c == '\'') {
					inSingleQuote = false;
				}
			}
			else if (inDoubleQuote) {
				if (c == '"') {
					inDoubleQuote = false;
				}
			}
			else {
				if (c == '\'') {
					inSingleQuote = true;
				}
				else if (c == '"') {
					inDoubleQuote = true;
				}
				else if (c == ':' && i + 1 < length && Character.isJavaIdentifierStart(query.charAt(i + 1))) {
					int j = i + 2;
					while (j < length && Character.isJavaIdentifierPart(query.charAt(j))) {
						j++;
					}
					String name = query.substring(i + 1, j);
					c = '?';
					i += name.length();
					List<Integer> indexList = indexMap.get(name);
					if (indexList == null) {
						indexList = new LinkedList<>();
					}
					indexList.add(index);
					indexMap.put(name, indexList);
					index++;
				}
			}
			stringBuilder.append(c);
		}
		return stringBuilder.toString();
	}

	private List<Integer> getIndexes(String name) {
		List<Integer> indexes = indexMap.get(name);
		if (indexes == null) {
			throw new IllegalArgumentException("Parameter not found: " + name);
		}
		return indexes;
	}

	public void setObject(String name, Object value) throws SQLException {
		List<Integer> indexes = getIndexes(name);
		for (Integer index : indexes) {
			preparedStatement.setObject(index, value);
		}
	}

	public void setString(String name, String value) throws SQLException {
		List<Integer> indexes = getIndexes(name);
		for (Integer index : indexes) {
			preparedStatement.setString(index, value);
		}
	}

	public void setInt(String name, int value) throws SQLException {
		List<Integer> indexes = getIndexes(name);
		for (Integer index : indexes) {
			preparedStatement.setInt(index, value);
		}
	}

	public void setLong(String name, Long value) throws SQLException {
		List<Integer> indexes = getIndexes(name);
		for (Integer index : indexes) {
			preparedStatement.setLong(index, value);
		}
	}

	public void setDate(String name, Date value) throws SQLException {
		List<Integer> indexes = getIndexes(name);
		for (Integer index : indexes) {
			preparedStatement.setDate(index, value);
		}
	}

	public PreparedStatement getStatement() {
		return preparedStatement;
	}

	public boolean execute() throws SQLException {
		return preparedStatement.execute();
	}

	public ResultSet executeQuery() throws SQLException {
		return preparedStatement.executeQuery();
	}

	public void close() throws SQLException {
		preparedStatement.close();
	}
}
