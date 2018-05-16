package de.tinf15b4.ihatestau.ihatestau_androidapp;

import org.junit.Assert;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.powermock.api.mockito.PowerMockito;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;

import java.util.Arrays;
import java.util.HashSet;

import de.tinf15b4.ihatestau.ihatestau_androidapp.util.FileManager;
import de.tinf15b4.ihatestau.ihatestau_androidapp.services.LearningHandler;
import de.tinf15b4.ihatestau.persistence.CameraSpotConfig;

@RunWith(PowerMockRunner.class)
@PrepareForTest(FileManager.class)
public class LearningHandlerTest {

    private LearningHandler classUnderTest;

    private static String EXIT_KRONAU = "Ausfahrt Kronau";
    private static String EXIT_BRUCHSAL = "Ausfahrt Bruchsal";
    private static String EXIT_KARLSRUHE_NORD = "Ausfahrt Karlsruhe Nord";
    private static String EXIT_KARLSRUHE_DURLACH = "Ausfahrt Karlsruhe Durlach";

    private static String CAMERA_BRUCHSAL_FR_HEIDELBERG = "Bruchsal Fahrtrichtung Heidelberg";
    private static String CAMERA_BRUCHSAL_FR_KARLSRUHE = "Bruchsal Fahrtrichtung Karlsruhe";
    private static String CAMERA_KARLSRUHE_NORD_FR_BASEL = "Karlsruhe Nord Fahrtrichtung Basel";
    private static String CAMERA_KARLSRUHE_NORD_FR_HEIDEBELRG = "Karlsruhe Nord Fahrtrichtung Heidelberg";

    private static CameraSpotConfig bruchsalFrKarlsruhe;
    private static CameraSpotConfig bruchsalFrHeidelberg;
    private static CameraSpotConfig karlsruheNordFrBasel;
    private static CameraSpotConfig karlsruheNordFrHeidelberg;

    @BeforeClass
    public static void initialSetUp() {

        bruchsalFrKarlsruhe = new CameraSpotConfig(CAMERA_BRUCHSAL_FR_KARLSRUHE,
                new HashSet<>(Arrays.asList(EXIT_KRONAU)),
                CAMERA_BRUCHSAL_FR_HEIDELBERG);

        bruchsalFrHeidelberg = new CameraSpotConfig(CAMERA_BRUCHSAL_FR_HEIDELBERG,
                new HashSet<>(Arrays.asList(EXIT_KARLSRUHE_NORD)),
                CAMERA_BRUCHSAL_FR_KARLSRUHE);

        karlsruheNordFrBasel = new CameraSpotConfig(CAMERA_KARLSRUHE_NORD_FR_BASEL,
                new HashSet<>(Arrays.asList(EXIT_BRUCHSAL)),
                CAMERA_KARLSRUHE_NORD_FR_HEIDEBELRG);

        karlsruheNordFrHeidelberg = new CameraSpotConfig(CAMERA_KARLSRUHE_NORD_FR_HEIDEBELRG,
                new HashSet<>(Arrays.asList(EXIT_KARLSRUHE_DURLACH)),
                CAMERA_KARLSRUHE_NORD_FR_BASEL);
    }

    @Before
    public void setUpBeforeTest() {
        classUnderTest = new LearningHandler();

        PowerMockito.mockStatic(FileManager.class);
        PowerMockito.when(FileManager.getCamera(CAMERA_KARLSRUHE_NORD_FR_HEIDEBELRG)).thenReturn(karlsruheNordFrHeidelberg);
        PowerMockito.when(FileManager.getCamera(CAMERA_KARLSRUHE_NORD_FR_BASEL)).thenReturn(karlsruheNordFrBasel);
        PowerMockito.when(FileManager.getCamera(CAMERA_BRUCHSAL_FR_HEIDELBERG)).thenReturn(bruchsalFrHeidelberg);
        PowerMockito.when(FileManager.getCamera(CAMERA_BRUCHSAL_FR_KARLSRUHE)).thenReturn(bruchsalFrKarlsruhe);
    }

    @Test
    public void AddTwoCamerasWithoutMatchingExit() {
        classUnderTest.addExit(EXIT_BRUCHSAL);
        classUnderTest.addCamera(CAMERA_BRUCHSAL_FR_HEIDELBERG);
        classUnderTest.addCamera(CAMERA_BRUCHSAL_FR_KARLSRUHE);
        classUnderTest.addExit(EXIT_KARLSRUHE_NORD);
        Assert.assertTrue(classUnderTest.getRelevantCameras().containsKey(CAMERA_BRUCHSAL_FR_KARLSRUHE));
        Assert.assertFalse(classUnderTest.getRelevantCameras().containsKey(CAMERA_BRUCHSAL_FR_HEIDELBERG));

        setUpBeforeTest();
        classUnderTest.addExit(EXIT_BRUCHSAL);
        classUnderTest.addCamera(CAMERA_BRUCHSAL_FR_KARLSRUHE);
        classUnderTest.addCamera(CAMERA_BRUCHSAL_FR_HEIDELBERG);
        classUnderTest.addExit(EXIT_KARLSRUHE_NORD);
        Assert.assertTrue(classUnderTest.getRelevantCameras().containsKey(CAMERA_BRUCHSAL_FR_KARLSRUHE));
        Assert.assertFalse(classUnderTest.getRelevantCameras().containsKey(CAMERA_BRUCHSAL_FR_HEIDELBERG));

        setUpBeforeTest();
        classUnderTest.addCamera(CAMERA_BRUCHSAL_FR_HEIDELBERG);
        classUnderTest.addExit(EXIT_BRUCHSAL);
        classUnderTest.addCamera(CAMERA_BRUCHSAL_FR_KARLSRUHE);
        classUnderTest.addExit(EXIT_KARLSRUHE_NORD);
        Assert.assertTrue(classUnderTest.getRelevantCameras().containsKey(CAMERA_BRUCHSAL_FR_KARLSRUHE));
        Assert.assertFalse(classUnderTest.getRelevantCameras().containsKey(CAMERA_BRUCHSAL_FR_HEIDELBERG));

        setUpBeforeTest();
        classUnderTest.addCamera(CAMERA_BRUCHSAL_FR_KARLSRUHE);
        classUnderTest.addExit(EXIT_BRUCHSAL);
        classUnderTest.addCamera(CAMERA_BRUCHSAL_FR_HEIDELBERG);
        classUnderTest.addExit(EXIT_KARLSRUHE_NORD);
        Assert.assertTrue(classUnderTest.getRelevantCameras().containsKey(CAMERA_BRUCHSAL_FR_KARLSRUHE));
        Assert.assertFalse(classUnderTest.getRelevantCameras().containsKey(CAMERA_BRUCHSAL_FR_HEIDELBERG));

        setUpBeforeTest();
        classUnderTest.addCamera(CAMERA_BRUCHSAL_FR_KARLSRUHE);
        classUnderTest.addCamera(CAMERA_BRUCHSAL_FR_HEIDELBERG);
        classUnderTest.addExit(EXIT_BRUCHSAL);
        classUnderTest.addExit(EXIT_KARLSRUHE_NORD);
        Assert.assertTrue(classUnderTest.getRelevantCameras().containsKey(CAMERA_BRUCHSAL_FR_KARLSRUHE));
        Assert.assertFalse(classUnderTest.getRelevantCameras().containsKey(CAMERA_BRUCHSAL_FR_HEIDELBERG));

        setUpBeforeTest();
        classUnderTest.addCamera(CAMERA_BRUCHSAL_FR_HEIDELBERG);
        classUnderTest.addCamera(CAMERA_BRUCHSAL_FR_KARLSRUHE);
        classUnderTest.addExit(EXIT_BRUCHSAL);
        classUnderTest.addExit(EXIT_KARLSRUHE_NORD);
        Assert.assertTrue(classUnderTest.getRelevantCameras().containsKey(CAMERA_BRUCHSAL_FR_KARLSRUHE));
        Assert.assertFalse(classUnderTest.getRelevantCameras().containsKey(CAMERA_BRUCHSAL_FR_HEIDELBERG));

    }

    @Test
    public void AddTwoCamerasWithMatchingExitAndLeaveRoute(){
        classUnderTest.addExit(EXIT_KRONAU);
        classUnderTest.addCamera(CAMERA_BRUCHSAL_FR_KARLSRUHE);
        classUnderTest.addCamera(CAMERA_BRUCHSAL_FR_HEIDELBERG);
        Assert.assertTrue(classUnderTest.getRelevantCameras().containsKey(CAMERA_BRUCHSAL_FR_KARLSRUHE));
        Assert.assertFalse(classUnderTest.getRelevantCameras().containsKey(CAMERA_BRUCHSAL_FR_HEIDELBERG));

        setUpBeforeTest();
        classUnderTest.addExit(EXIT_KRONAU);
        classUnderTest.addCamera(CAMERA_BRUCHSAL_FR_HEIDELBERG);
        classUnderTest.addCamera(CAMERA_BRUCHSAL_FR_KARLSRUHE);
        Assert.assertTrue(classUnderTest.getRelevantCameras().containsKey(CAMERA_BRUCHSAL_FR_KARLSRUHE));
        Assert.assertFalse(classUnderTest.getRelevantCameras().containsKey(CAMERA_BRUCHSAL_FR_HEIDELBERG));
    }

    @Test
    public void AddTwoCamerasWithMatchingExitAndDriveAlong(){
        classUnderTest.addExit(EXIT_KRONAU);
        classUnderTest.addCamera(CAMERA_BRUCHSAL_FR_KARLSRUHE);
        classUnderTest.addCamera(CAMERA_BRUCHSAL_FR_HEIDELBERG);
        classUnderTest.addExit(EXIT_KARLSRUHE_NORD);
        Assert.assertTrue(classUnderTest.getRelevantCameras().containsKey(CAMERA_BRUCHSAL_FR_KARLSRUHE));
        Assert.assertFalse(classUnderTest.getRelevantCameras().containsKey(CAMERA_BRUCHSAL_FR_HEIDELBERG));

        setUpBeforeTest();
        classUnderTest.addExit(EXIT_KRONAU);
        classUnderTest.addCamera(CAMERA_BRUCHSAL_FR_HEIDELBERG);
        classUnderTest.addCamera(CAMERA_BRUCHSAL_FR_KARLSRUHE);
        classUnderTest.addExit(EXIT_KARLSRUHE_NORD);
        Assert.assertTrue(classUnderTest.getRelevantCameras().containsKey(CAMERA_BRUCHSAL_FR_KARLSRUHE));
        Assert.assertFalse(classUnderTest.getRelevantCameras().containsKey(CAMERA_BRUCHSAL_FR_HEIDELBERG));
    }

    @Test
    public void AddTwoCamerasWithoutMatchingExitAndThenOneWithMatchingExit(){
        classUnderTest.addExit(EXIT_BRUCHSAL);
        classUnderTest.addCamera(CAMERA_BRUCHSAL_FR_KARLSRUHE);
        classUnderTest.addCamera(CAMERA_BRUCHSAL_FR_HEIDELBERG);
        classUnderTest.addCamera(CAMERA_KARLSRUHE_NORD_FR_BASEL);
        classUnderTest.addExit(EXIT_KARLSRUHE_NORD);
        Assert.assertTrue(classUnderTest.getRelevantCameras().containsKey(CAMERA_BRUCHSAL_FR_KARLSRUHE));
        Assert.assertTrue(classUnderTest.getRelevantCameras().containsKey(CAMERA_KARLSRUHE_NORD_FR_BASEL));
        Assert.assertFalse(classUnderTest.getRelevantCameras().containsKey(CAMERA_BRUCHSAL_FR_HEIDELBERG));
    }

}
