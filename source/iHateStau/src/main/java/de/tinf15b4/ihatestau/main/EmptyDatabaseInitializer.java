package de.tinf15b4.ihatestau.main;

import java.io.IOException;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;

import org.apache.commons.io.IOUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import de.tinf15b4.ihatestau.persistence.PersistenceBean;
import de.tinf15b4.ihatestau.persistence.User;

@ApplicationScoped
public class EmptyDatabaseInitializer {
    private static final Logger logger = LoggerFactory.getLogger(EmptyDatabaseInitializer.class);

    @Inject
    PersistenceBean persistenceBean;

    public void maybeInitialize() {
        if (persistenceBean.selectAll(User.class).size() != 0)
            return;

        try {
            String sql = IOUtils.toString(this.getClass().getResource("initialize-db.sql"), "UTF-8");
            persistenceBean.executeNativeQuery(sql);
        } catch (IOException e) {
            logger.error("Failed to read and execute database intialization script", e);
        }


    }
}
