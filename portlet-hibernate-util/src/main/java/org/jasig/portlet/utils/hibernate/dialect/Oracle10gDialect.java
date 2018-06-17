package org.jasig.portlet.utils.hibernate.dialect;

import java.sql.Types;

/**
 * A derived dialect of Oracle10gDialect that uses clobs over Oracle LONG.
 */
public class Oracle10gDialect extends org.hibernate.dialect.Oracle10gDialect {

    public Oracle10gDialect() {
        super();
        registerColumnType( Types.VARBINARY, 2000, "raw($l)" );
        registerColumnType( Types.VARBINARY, "blob" );

        registerColumnType( Types.LONGVARCHAR, "clob" );
        registerColumnType( Types.LONGVARBINARY, "blob" );

        registerColumnType( Types.VARCHAR, 4000, "varchar2($l char)" );
        registerColumnType( Types.VARCHAR, "clob" );   }
}
