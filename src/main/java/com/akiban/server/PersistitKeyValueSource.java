/**
 * END USER LICENSE AGREEMENT (“EULA”)
 *
 * READ THIS AGREEMENT CAREFULLY (date: 9/13/2011):
 * http://www.akiban.com/licensing/20110913
 *
 * BY INSTALLING OR USING ALL OR ANY PORTION OF THE SOFTWARE, YOU ARE ACCEPTING
 * ALL OF THE TERMS AND CONDITIONS OF THIS AGREEMENT. YOU AGREE THAT THIS
 * AGREEMENT IS ENFORCEABLE LIKE ANY WRITTEN AGREEMENT SIGNED BY YOU.
 *
 * IF YOU HAVE PAID A LICENSE FEE FOR USE OF THE SOFTWARE AND DO NOT AGREE TO
 * THESE TERMS, YOU MAY RETURN THE SOFTWARE FOR A FULL REFUND PROVIDED YOU (A) DO
 * NOT USE THE SOFTWARE AND (B) RETURN THE SOFTWARE WITHIN THIRTY (30) DAYS OF
 * YOUR INITIAL PURCHASE.
 *
 * IF YOU WISH TO USE THE SOFTWARE AS AN EMPLOYEE, CONTRACTOR, OR AGENT OF A
 * CORPORATION, PARTNERSHIP OR SIMILAR ENTITY, THEN YOU MUST BE AUTHORIZED TO SIGN
 * FOR AND BIND THE ENTITY IN ORDER TO ACCEPT THE TERMS OF THIS AGREEMENT. THE
 * LICENSES GRANTED UNDER THIS AGREEMENT ARE EXPRESSLY CONDITIONED UPON ACCEPTANCE
 * BY SUCH AUTHORIZED PERSONNEL.
 *
 * IF YOU HAVE ENTERED INTO A SEPARATE WRITTEN LICENSE AGREEMENT WITH AKIBAN FOR
 * USE OF THE SOFTWARE, THE TERMS AND CONDITIONS OF SUCH OTHER AGREEMENT SHALL
 * PREVAIL OVER ANY CONFLICTING TERMS OR CONDITIONS IN THIS AGREEMENT.
 */

package com.akiban.server;

import java.math.BigDecimal;
import java.math.BigInteger;

import com.akiban.ais.model.IndexColumn;
import com.akiban.qp.operator.Cursor;
import com.akiban.server.types.AkType;
import com.akiban.server.types.ValueSource;
import com.akiban.server.types.util.ValueHolder;
import com.akiban.util.AkibanAppender;
import com.akiban.util.ByteSource;
import com.persistit.Key;

public final class PersistitKeyValueSource implements ValueSource {

    // PersistitKeyValueSource interface

    public void attach(Key key, IndexColumn indexColumn) {
        attach(key, indexColumn.getPosition(), indexColumn.getColumn().getType().akType());
    }

    public void attach(Key key, int depth, AkType type) {
        if (type == AkType.INTERVAL_MILLIS || type == AkType.INTERVAL_MONTH)
            throw new UnsupportedOperationException();
        this.key = key;
        this.depth = depth;
        this.akType = type;
        clear();
    }
    
    public void attach(Key key) {
        this.key = key;
        clear();
    }

    // ValueSource interface

    @Override
    public boolean isNull() {
        return decode().isNull();
    }

    @Override
    public BigDecimal getDecimal() {
        return decode().getDecimal();
    }

    @Override
    public BigInteger getUBigInt() {
        return decode().getUBigInt();
    }

    @Override
    public ByteSource getVarBinary() {
        return decode().getVarBinary();
    }

    @Override
    public double getDouble() {
        return decode().getDouble();
    }

    @Override
    public double getUDouble() {
        return decode().getUDouble();
    }

    @Override
    public float getFloat() {
        return decode().getFloat();
    }

    @Override
    public float getUFloat() {
        return decode().getUFloat();
    }

    @Override
    public long getDate() {
        return decode().getDate();
    }

    @Override
    public long getDateTime() {
        return decode().getDateTime();
    }

    @Override
    public long getInt() {
        return decode().getInt();
    }

    @Override
    public long getLong() {
        return decode().getLong();
    }

    @Override
    public long getTime() {
        return decode().getTime();
    }

    @Override
    public long getTimestamp() {
        return decode().getTimestamp();
    }
    
    @Override
    public long getInterval_Millis() {
        throw new UnsupportedOperationException();
    }

    @Override
    public long getInterval_Month() {
        throw new UnsupportedOperationException();
    }
    
    @Override
    public long getUInt() {
        return decode().getUInt();
    }

    @Override
    public long getYear() {
        return decode().getYear();
    }

    @Override
    public String getString() {
        return  decode().getString();
    }

    @Override
    public String getText() {
        return decode().getText();
    }

    @Override
    public boolean getBool() {
        return decode().getBool();
    }

    @Override
    public Cursor getResultSet() {
        throw new UnsupportedOperationException();
    }
    
    @Override
    public void appendAsString(AkibanAppender appender, Quote quote) {
        // Can we optimize this at all?
        AkType type = getConversionType();
        quote.quote(appender, type);
        quote.append(appender, String.valueOf(decode()));
        quote.quote(appender, type);
    }

    @Override
    public AkType getConversionType() {
        return akType;
    }
    
    // object interface

    @Override
    public String toString() {
        return key.toString() + " bound to depth " + key.getDepth();
    }

    // for use by this class
    
    private ValueSource decode() {
        if (needsDecoding) {
            key.indexTo(depth);
            if (key.isNull()) {
                valueHolder.putNull();
            }
            else
            {
                switch (akType.underlyingType()) {
                    case BOOLEAN_AKTYPE:valueHolder.putBool(key.decodeBoolean());       break;
                    case LONG_AKTYPE:   valueHolder.putRaw(akType, key.decodeLong());   break;
                    case FLOAT_AKTYPE:  valueHolder.putRaw(akType, key.decodeFloat());  break;
                    case DOUBLE_AKTYPE: valueHolder.putRaw(akType, key.decodeDouble()); break;
                    case OBJECT_AKTYPE: valueHolder.putRaw(akType, key.decode());       break;
                    default: throw new UnsupportedOperationException(akType.name());
                }
            }
            needsDecoding = false;
        }
        return valueHolder;
    }
    
    private void clear() {
        needsDecoding = true;
    }

    // object state

    private Key key;
    private int depth;
    private AkType akType = AkType.UNSUPPORTED;
    private ValueHolder valueHolder = new ValueHolder();
    private boolean needsDecoding = true;
}
