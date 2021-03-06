package edu.hm.hafner.analysis;

import javax.swing.text.html.Option;
import java.io.Serializable;
import java.util.Optional;
import java.util.UUID;

import org.apache.commons.lang3.StringUtils;

import static edu.hm.hafner.util.IntegerParser.*;
import edu.hm.hafner.util.PathUtil;
import edu.umd.cs.findbugs.annotations.CheckForNull;

/**
 * Creates new {@link Issue issues} using the builder pattern. All properties that have not been set in the builder will
 * be set to their default value.
 * <p>Example:</p>
 * <blockquote><pre>
 * Issue issue = new IssueBuilder()
 *                      .setFileName("affected.file")
 *                      .setLineStart(0)
 *                      .setCategory("JavaDoc")
 *                      .setMessage("Missing JavaDoc")
 *                      .setSeverity(Severity.WARNING_LOW);
 * </pre></blockquote>
 *
 * @author Ullrich Hafner
 */
@SuppressWarnings({"InstanceVariableMayNotBeInitialized", "JavaDocMethod", "PMD.TooManyFields"})
public class IssueBuilder {
    private int lineStart = 0;
    private int lineEnd = 0;
    private int columnStart = 0;
    private int columnEnd = 0;
    @CheckForNull
    private LineRangeList lineRanges;
    @CheckForNull
    private String fileName;
    @CheckForNull
    private String directory;
    @CheckForNull
    private String category;
    @CheckForNull
    private String type;
    @CheckForNull
    private Severity severity;
    @CheckForNull
    private String message;
    @CheckForNull
    private String description;
    @CheckForNull
    private String packageName;
    @CheckForNull
    private String moduleName;
    @CheckForNull
    private String origin;
    @CheckForNull
    private String reference;
    @CheckForNull
    private String fingerprint;
    @CheckForNull
    private Serializable additionalProperties;

    private UUID id = UUID.randomUUID();

    public IssueBuilder setId(final UUID id) {
        this.id = id;
        return this;
    }

    public IssueBuilder setAdditionalProperties(@CheckForNull final Serializable additionalProperties) {
        this.additionalProperties = additionalProperties;
        return this;
    }

    public IssueBuilder setFingerprint(@CheckForNull final String fingerprint) {
        this.fingerprint = fingerprint;
        return this;
    }

    public IssueBuilder setFileName(@CheckForNull final String fileName) {
        if (StringUtils.isBlank(fileName)) {
            this.fileName = StringUtils.EMPTY;
        }
        else {
            this.fileName = new PathUtil().createAbsolutePath(directory, fileName);
        }

        return this;
    }

    public IssueBuilder setDirectory(@CheckForNull final String directory) {
        this.directory = directory;
        return this;
    }

    public IssueBuilder setLineStart(@CheckForNull final int lineStart) {
        this.lineStart = lineStart;
        return this;
    }

    public IssueBuilder setLineStart(@CheckForNull final String lineStart) {
        this.lineStart = parseInt(lineStart);
        return this;
    }

    public IssueBuilder setLineEnd(@CheckForNull final int lineEnd) {
        this.lineEnd = lineEnd;
        return this;
    }

    public IssueBuilder setLineEnd(@CheckForNull final String lineEnd) {
        this.lineEnd = parseInt(lineEnd);
        return this;
    }

    public IssueBuilder setColumnStart(final int columnStart) {
        this.columnStart = columnStart;
        return this;
    }

    public IssueBuilder setColumnStart(final String columnStart) {
        this.columnStart = parseInt(columnStart);
        return this;
    }

    public IssueBuilder setColumnEnd(final int columnEnd) {
        this.columnEnd = columnEnd;
        return this;
    }

    public IssueBuilder setColumnEnd(final String columnEnd) {
        this.columnEnd = parseInt(columnEnd);
        return this;
    }

    public IssueBuilder setCategory(@CheckForNull final String category) {
        this.category = category;
        return this;
    }

    public IssueBuilder setType(@CheckForNull final String type) {
        this.type = type;
        return this;
    }

    public IssueBuilder setPackageName(@CheckForNull final String packageName) {
        this.packageName = packageName;
        return this;
    }

    public IssueBuilder setModuleName(@CheckForNull final String moduleName) {
        this.moduleName = moduleName;
        return this;
    }

    public IssueBuilder setOrigin(@CheckForNull final String origin) {
        this.origin = origin;
        return this;
    }

    public IssueBuilder setReference(@CheckForNull final String reference) {
        this.reference = reference;
        return this;
    }

    public IssueBuilder setSeverity(@CheckForNull final Severity severity) {
        this.severity = severity;
        return this;
    }

    public IssueBuilder setMessage(@CheckForNull final String message) {
        this.message = message;
        return this;
    }

    public IssueBuilder setDescription(@CheckForNull final String description) {
        this.description = description;
        return this;
    }

    public IssueBuilder setLineRanges(final LineRangeList lineRanges) {
        this.lineRanges = new LineRangeList(lineRanges);
        return this;
    }

    /**
     * Initializes this builder with an exact copy of aal properties of the specified issue.
     *
     * @param copy
     *         the issue to copy the properties from
     *
     * @return the initialized builder
     */
    public IssueBuilder copy(final Issue copy) {
        fileName = copy.getFileName();
        lineStart = copy.getLineStart();
        lineEnd = copy.getLineEnd();
        columnStart = copy.getColumnStart();
        columnEnd = copy.getColumnEnd();
        lineRanges = copy.getLineRanges();
        category = copy.getCategory();
        type = copy.getType();
        severity = copy.getSeverity();
        message = copy.getMessage();
        description = copy.getDescription();
        packageName = copy.getPackageName();
        moduleName = copy.getModuleName();
        origin = copy.getOrigin();
        reference = copy.getReference();
        fingerprint = copy.getFingerprint();
        additionalProperties = copy.getAdditionalProperties();
        return this;
    }

    /**
     * Creates a new {@link Issue} based on the specified properties.
     *
     * @return the created issue
     */
    public Issue build() {
        Issue issue = new Issue(fileName, lineStart, lineEnd, columnStart, columnEnd, lineRanges, category, type,
                packageName, moduleName, severity, message, description, origin, reference, fingerprint,
                additionalProperties, id);
        id = UUID.randomUUID(); // make sure that multiple invocations will create different IDs
        return issue;
    }

    /**
     * Creates a new {@link Issue} based on the specified properties. The returned issue is wrapped in an {@link
     * Optional}.
     *
     * @return the created issue
     */
    public Optional<Issue> buildOptional() {
        return Optional.of(build());
    }
}