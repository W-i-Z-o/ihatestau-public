package de.tinf15b4.ihatestau.persistence;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.junit.After;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;

public class PersistenceBeanTest {

	private static PersistenceBean bean;

	@BeforeClass
	public static void setupClass() {
		bean = new PersistenceBean("testing");
	}

	@After
	public void cleanup() {
		bean.executeNativeQuery("TRUNCATE TABLE TestEntity");
	}

	@Test
	public void testPersistSelect() throws Exception {
		TestEntity entity = new TestEntity("testString");
		bean.persist(entity);
		List<TestEntity> result = bean.<TestEntity>selectAll(TestEntity.class);
		assertEquals(1, result.size());
		assertEquals(entity.getString(), result.get(0).getString());
	}

	@Test
	public void testSelect() throws Exception {
		TestEntity entity = new TestEntity("testString");
		TestEntity entity2 = new TestEntity("testString2");
		TestEntity entity3 = new TestEntity("testString3");
		bean.persist(entity);
		bean.persist(entity2);
		bean.persist(entity3);
		List<TestEntity> result = bean.<TestEntity>selectAll(TestEntity.class);
		assertEquals(3, result.size());

		Map<String, Object> params = new HashMap<>();
		params.put("string", "testString2");
		result = bean.<TestEntity>select("SELECT soe FROM TestEntity soe WHERE soe.string = :string", params);
		assertEquals(1, result.size());
		assertEquals("testString2", result.get(0).getString());
	}

	@Test
	public void testPersistWithNoEntity() throws Exception {
		try {
			bean.persist(null);
			Assert.fail();
		} catch (Exception e) {
		}

		try {
			bean.persist(new Object());
			Assert.fail();
		} catch (Exception e) {
		}
	}

	@Test
	public void testUpdate() throws Exception {
		TestEntity entity = new TestEntity("testString");
		bean.persist(entity);
		List<TestEntity> result = bean.<TestEntity>selectAll(TestEntity.class);
		TestEntity selected = result.get(0);
		selected.setString("AnotherTest");
		TestEntity merged = bean.merge(selected);
		assertEquals("AnotherTest", merged.getString());
		result = bean.<TestEntity>selectAll(TestEntity.class);
		assertEquals(1, result.size());
		assertEquals(merged.getString(), result.get(0).getString());
	}

	@Test
	public void testDelete() throws Exception {
		TestEntity entity = new TestEntity("testString");
		bean.persist(entity);
		List<TestEntity> result = bean.<TestEntity>selectAll(TestEntity.class);
		TestEntity selected = result.get(0);
		bean.delete(selected);
		result = bean.<TestEntity>selectAll(TestEntity.class);
		assertEquals(0, result.size());
	}

}
