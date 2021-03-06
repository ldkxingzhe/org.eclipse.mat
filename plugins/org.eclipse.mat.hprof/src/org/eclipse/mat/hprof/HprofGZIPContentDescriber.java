/*******************************************************************************
 * Copyright (c) 2019 IBM Corporation.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.mat.hprof;

import java.io.IOException;
import java.io.InputStream;
import java.util.zip.GZIPInputStream;
import java.util.zip.ZipException;

import org.eclipse.core.runtime.QualifiedName;
import org.eclipse.core.runtime.content.IContentDescriber;
import org.eclipse.core.runtime.content.IContentDescription;

public class HprofGZIPContentDescriber implements IContentDescriber
{
    private static final QualifiedName[] QUALIFIED_NAMES = new QualifiedName[] { new QualifiedName("java-heap-dump", //$NON-NLS-1$
                    "hprof-gzip") }; //$NON-NLS-1$

    public int describe(InputStream contents, IContentDescription description) throws IOException
    {
        try
        {
            return AbstractParser.readVersion(new GZIPInputStream(contents)) != null ? VALID : INVALID;
        }
        catch (ZipException e)
        {
            /*
             * Distinguish a zip format error, which is definitely unsuitable,
             * from an IOException, which might just be a read error and so we do not know.
             */
            return INVALID;
        }
    }

    public QualifiedName[] getSupportedOptions()
    {
        return QUALIFIED_NAMES;
    }

}
