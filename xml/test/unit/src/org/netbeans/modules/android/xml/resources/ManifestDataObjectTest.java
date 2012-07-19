package org.netbeans.modules.android.xml.resources;

import org.netbeans.modules.android.xml.manifest.ManifestDataObject;
import junit.framework.TestCase;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.XMLFileSystem;
import org.openide.loaders.DataObject;

public class ManifestDataObjectTest extends TestCase {

    public ManifestDataObjectTest(String testName) {
        super(testName);
    }

    public void testDataObject() throws Exception {
        XMLFileSystem fs = new XMLFileSystem(
                ManifestDataObjectTest.class.getResource("fs-layer.xml"));
        FileObject template = fs.getRoot().getFileObject("Sample/AndroidManifest.xml");
        assertNotNull("Template file shall be found", template);

        DataObject obj = DataObject.find(template);
        assertEquals("It is our data object", ManifestDataObject.class, obj.getClass());
    }
}
