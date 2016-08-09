package com.gradle.experience

import com.android.annotations.NonNull
import com.android.build.api.transform.Format
import com.android.build.api.transform.QualifiedContent
import com.android.build.api.transform.Transform
import com.android.build.gradle.AndroidConfig
import com.android.build.gradle.AppExtension
import com.android.build.gradle.AppPlugin
import com.android.build.gradle.BaseExtension
import com.android.build.gradle.internal.TaskContainerAdaptor
import com.android.build.gradle.internal.api.ApplicationVariantImpl
import com.android.build.gradle.internal.core.GradleVariantConfiguration
import com.android.build.gradle.internal.pipeline.IntermediateFolderUtils
import com.android.build.gradle.internal.pipeline.OriginalStream
import com.android.build.gradle.internal.pipeline.TransformManager
import com.android.build.gradle.internal.pipeline.TransformTask
import com.android.build.gradle.internal.scope.AndroidTask
import com.android.build.gradle.internal.scope.VariantScope
import com.android.build.gradle.internal.variant.ApplicationVariantData
import com.android.builder.model.AndroidProject
import com.android.utils.FileUtils
import com.android.utils.StringHelper
import org.gradle.api.Plugin
import org.gradle.api.Project

import static com.android.utils.FileUtils.deleteIfExists
import static com.android.utils.StringHelper.capitalize

/**
 * author:lizhangqu
 * date:2016/7/19
 */
class PluginImpl implements Plugin<Project> {
    @Override
    void apply(Project project) {
        project.extensions.create(PluginExtension.PLUGIN_EXTENTION, PluginExtension, project)
        def isApp = project.plugins.hasPlugin(AppPlugin.class)
        project.afterEvaluate {
            if (isApp) {
                BaseExtension android = project.extensions.getByType(AppExtension)
                android.applicationVariants.each { ApplicationVariantImpl variant ->
                    ApplicationVariantData applicationVariantData = variant.variantData
                    GradleVariantConfiguration config = applicationVariantData.variantConfiguration;
                    VariantScope scope = applicationVariantData.scope
                    AndroidConfig extension = scope.globalScope.extension;
                    TransformManager transformManager = scope.transformManager
                    project.logger.error "Intermediates:${scope.globalScope.getIntermediatesDir()}"
                    project.logger.error "variant:${variant}"
                    project.logger.error "variantData:${variant.variantData}"
                    project.logger.error "config:${config}"
                    project.logger.error "scope:${scope}"
                    project.logger.error "extension:${extension}"
                    project.logger.error "transformManager:${transformManager}"

//                project.logger.error "getSourceFoldersJavaResDestinationDir:${scope.getSourceFoldersJavaResDestinationDir()}"
//                project.logger.error "getMergeNativeLibsOutputDir:${scope.getMergeNativeLibsOutputDir()}"
//                project.logger.error "getJarMergingOutputFile:${scope.getJarMergingOutputFile()}"


                    def dexTask = project.tasks.findByName("transformClassesWithDexFor${variant.name.capitalize()}")
                    def jarMergingTask = project.tasks.findByName("transformClassesWithJarMergingFor${variant.name.capitalize()}")
                    def proguardTask = project.tasks.findByName("transformClassesAndResourcesWithProguardFor${variant.name.capitalize()}")

                    project.logger.error "dexTask:${dexTask}"
                    project.logger.error "jarMergingTask:${jarMergingTask}"
                    project.logger.error "proguardTask:${proguardTask}"

                    TaskContainerAdaptor tasks = new TaskContainerAdaptor(project.getTasks())
                    if (jarMergingTask != null && dexTask != null) {
                        TransformTask jarMergingTransformTask = (TransformTask) tasks.named(jarMergingTask.getName())
                        Transform jarMergingTransform = jarMergingTransformTask.transform
                        File outRootFolder = FileUtils.join(project.buildDir, StringHelper.toStrings(
                                AndroidProject.FD_INTERMEDIATES,
                                "transforms",
                                jarMergingTransform.getName(),
                                scope.getDirectorySegments()));

                        project.logger.error "outRootFolder:${outRootFolder}"

                        Set<QualifiedContent.ContentType> types = jarMergingTransform.getOutputTypes();
                        Set<QualifiedContent.Scope> scopes = jarMergingTransform.getScopes()
                        Format format = Format.JAR
                        File file = IntermediateFolderUtils.getContentLocation(outRootFolder, "combined", types, scopes, format);
                        FileUtils.mkdirs(file.getParentFile());
                        deleteIfExists(file);
                        project.logger.error "file:${file.absolutePath}"
                        project.logger.error "file exits:${file.exists()}"



                        transformManager.addStream(OriginalStream.builder()
                                .addContentTypes(TransformManager.CONTENT_JARS)
                                .addScope(QualifiedContent.Scope.PROJECT)
                                .setJar(file)
                                .setDependency(jarMergingTask.getName())
                                .build());

                        TestTransform testTransform = new TestTransform(project);
                        AndroidTask<TransformTask> testTask = transformManager.addTransform(tasks, scope, testTransform)
                        TransformTask transformTask = (TransformTask) tasks.named(testTask.getName())
                        dexTask.dependsOn(testTask.getName())


                        project.logger.error "dexTask inputs:${dexTask.inputs.files.files}"

                    }

                    if (proguardTask != null && dexTask != null) {
                        TransformTask proguardTransformTask = (TransformTask) tasks.named(proguardTask.getName())
                        Transform proguardTransform = proguardTransformTask.transform
                        File outRootFolder = FileUtils.join(project.buildDir, StringHelper.toStrings(
                                AndroidProject.FD_INTERMEDIATES,
                                "transforms",
                                proguardTransform.getName(),
                                scope.getDirectorySegments()));

                        project.logger.error "outRootFolder:${outRootFolder}"

                        Set<QualifiedContent.ContentType> types = proguardTransform.getInputTypes();
                        Set<QualifiedContent.Scope> scopes = proguardTransform.getScopes()
                        Format format = Format.JAR
                        File file = IntermediateFolderUtils.getContentLocation(outRootFolder, "main", types, scopes, format);
                        FileUtils.mkdirs(file.getParentFile());
                        deleteIfExists(file);
                        project.logger.error "file:${file.absolutePath}"
                        project.logger.error "file exits:${file.exists()}"

//                    transformManager.addStream(OriginalStream.builder()
//                            .addContentTypes(TransformManager.CONTENT_JARS)
//                            .addScope(QualifiedContent.Scope.PROJECT)
//                            .setJar(file)
//                            .setDependency(proguardTask.getName())
//                            .build());

                        TestTransform testTransform = new TestTransform(project);
                        AndroidTask<TransformTask> testTask = transformManager.addTransform(tasks, scope, testTransform)
                        TransformTask transformTask = (TransformTask) tasks.named(testTask.getName())
                        dexTask.dependsOn(testTask.getName())


                        project.logger.error "dexTask inputs:${dexTask.inputs.files.files}"

                    }
                    //
//
////                def mergeJavaResTask = project.tasks.findByName("transformResourcesWithMergeJavaResFor${variant.name.capitalize()}")
//
//                project.logger.error "mergeJarTask:${dexTask}"
//                project.logger.error "inputs:${dexTask.inputs.files.files}"
//                Set<Task> dependencies = dexTask.taskDependencies.getDependencies(dexTask)
//                project.logger.error "dependencies:${dependencies}"

//                project.logger.error "transformTask:${transformTask}"
//                testTask.optionalDependsOn(tasks, dependencies)
//                dexTask.dependsOn testTask.getName()

//                if (mergeJavaResTask != null) {
//                    mergeJavaResTask.dependsOn(transformTask)
//                }

//                if (proguardTask != null) {
//                    transformTask.dependsOn(proguardTask)
//                }
//
//                if (dexTask != null) {
//                    dexTask.dependsOn(transformTask)
//                }

//                def dexTask = project.tasks.findByName("transformClassesWithDexFor${variant.name.capitalize()}")
//                dexTask.dependsOn(testTask.getName())
                }
            }
        }

    }

    @NonNull
    private static String getTaskNamePrefix(@NonNull Transform transform) {
        StringBuilder sb = new StringBuilder(100);
        sb.append("transform");

        Iterator<QualifiedContent.ContentType> iterator = transform.getInputTypes().iterator();
        // there's always at least one
        sb.append(capitalize(iterator.next().name().toLowerCase(Locale.getDefault())));
        while (iterator.hasNext()) {
            sb.append("And").append(capitalize(
                    iterator.next().name().toLowerCase(Locale.getDefault())));
        }

        sb.append("With").append(capitalize(transform.getName())).append("For");

        return sb.toString();
    }

}


