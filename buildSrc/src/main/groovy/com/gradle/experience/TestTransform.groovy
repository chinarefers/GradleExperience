package com.gradle.experience

import com.android.build.api.transform.*
import com.android.build.gradle.internal.pipeline.TransformManager
import org.apache.commons.io.FileUtils
import org.gradle.api.Project

/**
 * author:lizhangqu
 * date:2016/7/19
 */
class TestTransform extends Transform {
    private final Project project

    public TestTransform(Project project) {
        this.project = project

    }

    @Override
    String getName() {
        return "beforeDex"
    }

    @Override
    Set<QualifiedContent.ContentType> getInputTypes() {
        return TransformManager.CONTENT_JARS;
    }

    @Override
    Set<QualifiedContent.Scope> getScopes() {
        return TransformManager.SCOPE_FULL_PROJECT;
    }

    @Override
    boolean isIncremental() {
        return false;
    }

    @Override
    void transform(TransformInvocation transformInvocation) throws TransformException, InterruptedException, IOException {

        project.logger.println()
        project.logger.error "=========execute==============="

        Context context = transformInvocation.getContext()
        Collection<TransformInput> inputs = transformInvocation.getInputs()
        Collection<SecondaryInput> secondaryInputs = transformInvocation.getSecondaryInputs()
        Collection<TransformInput> referencedInputs = transformInvocation.getReferencedInputs()
        TransformOutputProvider outputProvider = transformInvocation.getOutputProvider()
        boolean isIncremental = transformInvocation.isIncremental()


        project.logger.error "context:${context}"
        project.logger.error "inputs:${inputs}"
        project.logger.error "secondaryInputs:${secondaryInputs}"
        project.logger.error "referencedInputs:${referencedInputs}"
        project.logger.error "outputProvider:${outputProvider}"
        project.logger.error "isIncremental:${isIncremental}"

        inputs.each { TransformInput input ->

            /**
             * jar遍历
             */
            input.jarInputs.each { JarInput jarInput ->
                project.logger.error "JarInputs:${jarInput.name}\n${jarInput.file.absolutePath}"
                File dest = outputProvider.getContentLocation(jarInput.name, jarInput.contentTypes, jarInput.scopes, Format.JAR);
                FileUtils.copyFile(jarInput.file, dest)
            }

            /**
             * 文件夹遍历
             */
            input.directoryInputs.each { DirectoryInput directoryInput ->
                project.logger.error "DirectoryInputs:${directoryInput.name}\n${directoryInput.file.absolutePath}"

                File dest = outputProvider.getContentLocation(directoryInput.name, directoryInput.contentTypes, directoryInput.scopes, Format.DIRECTORY);
                FileUtils.copyDirectory(directoryInput.file, dest);

            }

        }

    }


}


