package com.tom.aspirated.service;

import java.lang.reflect.Method;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.jdbc.core.namedparam.NamedParameterUtils;
import org.springframework.jdbc.core.namedparam.ParsedSql;
import org.springframework.stereotype.Service;

import com.tom.aspirated.sqlstatements.SqlStatements;
import com.tom.utils.SqlUtils;

/**
 * @author TommyDeng <250575979@qq.com>
 * @version 创建时间：2016年9月28日 下午2:34:43
 *
 */

@Service
public class DataAccessServiceImpl implements DataAccessService {

	@Autowired
	NamedParameterJdbcTemplate namedParameterJdbcTemplate;

	@Override
	public <T> T queryForOneObject(String sqlName, Map<String, Object> paramMap, Class<T> cls) {

		paramMap = SqlUtils.revertKeyUpcase(paramMap);

		List<T> resultList = namedParameterJdbcTemplate.query(SqlStatements.get(sqlName), paramMap, new RowMapper<T>() {

			@Override
			@SuppressWarnings("unchecked")
			public T mapRow(ResultSet rs, int rowNum) throws SQLException {
				// 取第一列
				// H2 [Not supported] without throw unsupported,fuck!
				// return rs.getObject(1, cls);
				return (T) rs.getObject(1);
			}

		});
		if (resultList == null || resultList.isEmpty()) {
			return null;
		} else {
			return resultList.get(0);// 取第一行
		}
	}

	@Override
	public Map<String, Object> queryForOneRow(String sqlName, Map<String, Object> paramMap) {
		paramMap = SqlUtils.revertKeyUpcase(paramMap);
		return namedParameterJdbcTemplate.queryForMap(SqlStatements.get(sqlName), paramMap);
	}

	@Override
	public int insertSingle(String tableName, Map<String, Object> paramMap) throws Exception {
		tableName = tableName.toUpperCase();
		paramMap = SqlUtils.revertKeyUpcase(paramMap);
		// setNullIfPlaceHolderNotExistsInParamMap(insertSql, paramMap);
		return namedParameterJdbcTemplate.update(getInsertSqlByTableNameAndParamMap(tableName, paramMap), paramMap);
	}

	@Override
	public int updateSingle(String tableName, Map<String, Object> paramMap) throws Exception {
		tableName = tableName.toUpperCase();
		paramMap = SqlUtils.revertKeyUpcase(paramMap);
		return namedParameterJdbcTemplate.update(getUpdateSqlByTableNameAndParamMap(tableName, paramMap), paramMap);
	}

	@Override
	public int mergeSingle(String tableName, Map<String, Object> paramMap) throws Exception {
		tableName = tableName.toUpperCase();
		paramMap = SqlUtils.revertKeyUpcase(paramMap);
		return namedParameterJdbcTemplate.update(getMergeSqlByTableNameAndParamMap(tableName, paramMap), paramMap);
	}

	/**
	 * 根据表名生成全字段insert语句
	 * 
	 * @param tableName
	 * @return
	 * @throws Exception
	 */
	private String getInsertSqlByTableNameAndParamMap(String tableName, Map<String, Object> paramMap) throws Exception {
		List<Map<String, Object>> columnsDescList = getColumnsDescListByTableName(tableName);

		StringBuilder returnSql = new StringBuilder();
		StringBuilder paramPlaceholder = new StringBuilder();

		returnSql.append("insert into " + tableName);
		returnSql.append("(");
		paramPlaceholder.append("(");

		for (Map<String, Object> columnDesc : columnsDescList) {
			// columnDesc.get("FIELD");// COLUMN_NAME
			// columnDesc.get("TYPE");// VARCHAR(2500)
			// columnDesc.get("NULL");// YES or NO
			// columnDesc.get("KEY");// PRI or ''
			// columnDesc.get("DEFAULT");// NULL

			String fieldName = (String) columnDesc.get("FIELD");

			// paramMap contains then set field value
			if (paramMap.containsKey(fieldName)) {
				returnSql.append(fieldName + ",");
				paramPlaceholder.append(":" + fieldName + ",");
			}
		}
		returnSql.delete(returnSql.length() - 1, returnSql.length());
		paramPlaceholder.delete(paramPlaceholder.length() - 1, paramPlaceholder.length());

		returnSql.append(")");
		paramPlaceholder.append(")");

		returnSql.append(" values ");
		returnSql.append(paramPlaceholder);

		return returnSql.toString().toUpperCase();
	}

	/**
	 * 根据表名生成paramMap中存在的字段update语句
	 * 
	 * @param tableName
	 * @param paramMap
	 * @return
	 * @throws Exception
	 */
	private String getUpdateSqlByTableNameAndParamMap(String tableName, Map<String, Object> paramMap) throws Exception {
		List<Map<String, Object>> columnsDescList = getColumnsDescListByTableName(tableName);

		StringBuilder returnSql = new StringBuilder();

		StringBuilder whereClauseSql = new StringBuilder();

		boolean setFeildEmpty = true;
		returnSql.append("update " + tableName);
		returnSql.append(" set ");

		for (Map<String, Object> columnDesc : columnsDescList) {
			// columnDesc.get("FIELD");// COLUMN_NAME
			// columnDesc.get("TYPE");// VARCHAR(2500)
			// columnDesc.get("NULL");// YES or NO
			// columnDesc.get("KEY");// PRI or ''
			// columnDesc.get("DEFAULT");// NULL

			String fieldName = (String) columnDesc.get("FIELD");

			// PK for where clause
			if ("PRI".equals(columnDesc.get("KEY"))) {
				if (paramMap.containsKey(fieldName)) {
					// only support for 1 pk
					whereClauseSql.append(fieldName + "=:" + fieldName);
				} else {// PK not exists in paramMap
					throw new Exception("PK not exists in paramMap :" + fieldName);
				}
			} else {
				// paramMap contains then set field value
				if (paramMap.containsKey(fieldName)) {
					returnSql.append(fieldName + "=:" + fieldName + ",");
					setFeildEmpty = false;
				}
			}
		}

		if (setFeildEmpty) {
			throw new Exception("update feild empty in paramMap");
		}
		// delete ,
		returnSql.delete(returnSql.length() - 1, returnSql.length());
		returnSql.append(" where ").append(whereClauseSql);
		return returnSql.toString().toUpperCase();
	}

	private String getMergeSqlByTableNameAndParamMap(String tableName, Map<String, Object> paramMap) throws Exception {
		// MERGE INTO SYS_USERINFO_WX(OPENID,NICKNAME) KEY(OPENID )
		// VALUES(:OPENID, :NICKNAME)
		List<Map<String, Object>> columnsDescList = getColumnsDescListByTableName(tableName);

		StringBuilder returnSql = new StringBuilder();

		StringBuilder paramPlaceholder = new StringBuilder();
		StringBuilder keyPlaceholder = new StringBuilder();

		boolean setFeildEmpty = true;
		returnSql.append("merge into " + tableName);
		returnSql.append("(");
		paramPlaceholder.append("(");

		for (Map<String, Object> columnDesc : columnsDescList) {
			// columnDesc.get("FIELD");// COLUMN_NAME
			// columnDesc.get("TYPE");// VARCHAR(2500)
			// columnDesc.get("NULL");// YES or NO
			// columnDesc.get("KEY");// PRI or ''
			// columnDesc.get("DEFAULT");// NULL

			String fieldName = (String) columnDesc.get("FIELD");

			if ("PRI".equals(columnDesc.get("KEY"))) {
				if (paramMap.containsKey(fieldName)) {
					// only support for 1 pk
					keyPlaceholder.append("(" + fieldName + ")");
				} else {// PK not exists in paramMap
					throw new Exception("PK not exists in paramMap" + fieldName);
				}
			}

			// paramMap contains then set field value
			if (paramMap.containsKey(fieldName)) {
				returnSql.append(fieldName + ",");
				paramPlaceholder.append(":" + fieldName + ",");

				setFeildEmpty = false;
			}

		}

		if (setFeildEmpty) {
			throw new Exception("update feild empty in paramMap");
		}

		returnSql.delete(returnSql.length() - 1, returnSql.length());
		paramPlaceholder.delete(paramPlaceholder.length() - 1, paramPlaceholder.length());

		returnSql.append(")");
		paramPlaceholder.append(")");

		returnSql.append(" key ");
		returnSql.append(keyPlaceholder);
		returnSql.append(" values ");
		returnSql.append(paramPlaceholder);

		return returnSql.toString();
	}

	/**
	 * 获取表字段说明
	 * 
	 * @param tableName
	 * @return
	 * @throws Exception
	 */
	private List<Map<String, Object>> getColumnsDescListByTableName(String tableName) throws Exception {
		String descTableSql = "show columns from " + tableName;
		Map<String, Object> paramMap = new HashMap<>();
		// paramMap.put("TABLE_NAME", tableName);

		List<Map<String, Object>> columnsDescList = namedParameterJdbcTemplate.queryForList(descTableSql, paramMap);
		if (columnsDescList == null || columnsDescList.size() == 0) {
			throw new Exception("table not exsits :" + tableName);
		}
		return columnsDescList;
	}

	/**
	 * 将paramMap中不存在的于sql中的place holder赋值为null
	 * 
	 * @param sql
	 * @param paramMap
	 * @throws Exception
	 */
	@SuppressWarnings("unused")
	private static void setNullIfPlaceHolderNotExistsInParamMap(String sql, Map<String, Object> paramMap)
			throws Exception {
		ParsedSql parsedSql = NamedParameterUtils.parseSqlStatement(sql);
		// ParsedSql in org.springframework.jdbc.core.namedparam
		// List<String> paramNames = parsedSql.getParameterNames();

		// Method[] methods = ParsedSql.class.getMethods();
		// Method[] methodsUnvisiable = ParsedSql.class.getDeclaredMethods();
		// method not visible, use reflection to access!
		Method method = ParsedSql.class.getDeclaredMethod("getParameterNames");
		method.setAccessible(true);

		@SuppressWarnings("unchecked")
		List<String> paramNames = (List<String>) method.invoke(parsedSql);

		for (String paramName : paramNames) {
			if (!paramMap.containsKey(paramName)) {
				paramMap.put(paramName, null);
			}
		}
	}

	@Override
	public Map<String, Object> queryForOneRowAllColumn(String tableName, Object pk) throws Exception {
		tableName = tableName.toUpperCase();
		String sql = "select * from " + tableName + " where ";
		List<Map<String, Object>> columnsDescList = getColumnsDescListByTableName(tableName);

		String pkFieldName = "";
		for (Map<String, Object> columnsDesc : columnsDescList) {
			if ("PRI".equals((String) columnsDesc.get("KEY"))) {
				pkFieldName = (String) columnsDesc.get("FIELD");
				break;
			}
		}

		sql += pkFieldName + " = :" + pkFieldName;
		Map<String, Object> paramMap = new HashMap<>();
		paramMap.put(pkFieldName, pk);

		List<Map<String, Object>> mapList = namedParameterJdbcTemplate.queryForList(sql, paramMap);
		if (mapList == null || mapList.size() == 0) {
			return null;
		} else {
			return mapList.get(0);
		}

	}

	@Override
	public int deleteRowById(String tableName, Object pk) throws Exception {
		tableName = tableName.toUpperCase();
		String sql = "delete from " + tableName + " where ";
		List<Map<String, Object>> columnsDescList = getColumnsDescListByTableName(tableName);

		String pkFieldName = "";
		for (Map<String, Object> columnsDesc : columnsDescList) {
			if ("PRI".equals((String) columnsDesc.get("KEY"))) {
				pkFieldName = (String) columnsDesc.get("FIELD");
				break;
			}
		}

		sql += pkFieldName + " = :" + pkFieldName;
		Map<String, Object> paramMap = new HashMap<>();
		paramMap.put(pkFieldName, pk);
		return namedParameterJdbcTemplate.update(sql, paramMap);
	}

	@Override
	public int update(String sqlName, Map<String, Object> paramMap) {
		paramMap = SqlUtils.revertKeyUpcase(paramMap);
		return namedParameterJdbcTemplate.update(SqlStatements.get(sqlName), paramMap);
	}

	@Override
	public List<Map<String, Object>> queryMapList(String sqlName, Map<String, Object> paramMap) {
		paramMap = SqlUtils.revertKeyUpcase(paramMap);
		return namedParameterJdbcTemplate.queryForList(SqlStatements.get(sqlName), paramMap);
	}

	@Override
	public List<Map<String, Object>> queryMapList(String sqlName) {
		return queryMapList(sqlName, null);
	}

}
