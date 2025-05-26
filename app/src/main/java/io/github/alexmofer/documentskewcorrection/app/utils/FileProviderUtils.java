package io.github.alexmofer.documentskewcorrection.app.utils;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;

import androidx.annotation.NonNull;
import androidx.core.content.FileProvider;

import java.io.File;

import io.github.alexmofer.documentskewcorrection.app.BuildConfig;

/**
 * FileProvider辅助
 * Created by Alex on 2025/5/26.
 */
public class FileProviderUtils {

    private FileProviderUtils() {
        //no instance
    }

    /**
     * Return a content URI for a given {@link File}. Specific temporary
     * permissions for the content URI can be set with
     * {@link Context#grantUriPermission(String, Uri, int)}, or added
     * to an {@link Intent} by calling {@link Intent#setData(Uri) setData()} and then
     * {@link Intent#setFlags(int) setFlags()}; in both cases, the applicable flags are
     * {@link Intent#FLAG_GRANT_READ_URI_PERMISSION} and
     * {@link Intent#FLAG_GRANT_WRITE_URI_PERMISSION}. A FileProvider can only return a
     * <code>content</code> {@link Uri} for file paths defined in their <code>&lt;paths&gt;</code>
     * meta-data element. See the Class Overview for more information.
     *
     * @param context A {@link Context} for the current component.
     * @param file    A {@link File} pointing to the filename for which you want a
     *                <code>content</code> {@link Uri}.
     * @return A content URI for the file.
     * @throws IllegalArgumentException When the given {@link File} is outside
     *                                  the paths supported by the provider.
     */
    public static Uri getUriForFile(@NonNull Context context, @NonNull File file) {
        return FileProvider.getUriForFile(context,
                BuildConfig.APPLICATION_ID + ".provider.file", file);
    }

    /**
     * Return a content URI for a given {@link File}. Specific temporary
     * permissions for the content URI can be set with
     * {@link Context#grantUriPermission(String, Uri, int)}, or added
     * to an {@link Intent} by calling {@link Intent#setData(Uri) setData()} and then
     * {@link Intent#setFlags(int) setFlags()}; in both cases, the applicable flags are
     * {@link Intent#FLAG_GRANT_READ_URI_PERMISSION} and
     * {@link Intent#FLAG_GRANT_WRITE_URI_PERMISSION}. A FileProvider can only return a
     * <code>content</code> {@link Uri} for file paths defined in their <code>&lt;paths&gt;</code>
     * meta-data element. See the Class Overview for more information.
     *
     * @param context     A {@link Context} for the current component.
     * @param file        A {@link File} pointing to the filename for which you want a
     *                    <code>content</code> {@link Uri}.
     * @param displayName The filename to be displayed. This can be used if the original filename
     *                    is undesirable.
     * @return A content URI for the file.
     * @throws IllegalArgumentException When the given {@link File} is outside
     *                                  the paths supported by the provider.
     */
    public static Uri getUriForFile(@NonNull Context context, @NonNull File file,
                                    @NonNull String displayName) {
        return FileProvider.getUriForFile(context,
                BuildConfig.APPLICATION_ID + ".provider.file", file, displayName);
    }
}
