package act6;

import static org.junit.Assert.*;

import org.activiti.engine.impl.db.DbSchemaCreate;
import org.junit.Test;

public class PrepareDatabase {

	
	@Test
	public void testPrepareDb() throws Exception {
		DbSchemaCreate.main(null);
	}
}
