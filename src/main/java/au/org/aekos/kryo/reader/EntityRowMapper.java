package au.org.aekos.kryo.reader;

import java.sql.ResultSet;
import java.sql.SQLException;

import org.springframework.jdbc.core.RowMapper;

import au.org.aekos.core.ingest.serialisation.AekosSerialisation;
import au.org.aekos.core.values.Entity;

public class EntityRowMapper implements RowMapper<Entity> {

	private static final String ENTITY_COLUMN_NAME = "entry";

	@Override
	public Entity mapRow(ResultSet rs, int i) throws SQLException {
		return (Entity) AekosSerialisation.getInstance().deserialise(rs.getBytes(ENTITY_COLUMN_NAME));
	}
}
