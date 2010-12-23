<?xml version="1.0" encoding="ISO-8859-1"?>
<xsl:stylesheet version="1.0" xmlns:xsl="http://www.w3.org/1999/XSL/Transform">
	<xsl:param name="startToc"/>
	<xsl:param name="tocIndex"/>
	
  	<xsl:template match="/">
		<html>
			<body>
				<xsl:for-each select="/html/body/p[@class='r_norm' or @class='r_head']">
					<xsl:element name="p">
						<xsl:if test="@class='r_head'">
							<xsl:attribute name="id">
								<xsl:value-of select="$startToc"/>
								<xsl:value-of select="$tocIndex + position()"/>
							</xsl:attribute>
						</xsl:if>
						<xsl:attribute name="class">
							<xsl:value-of select="@class"/>
						</xsl:attribute>
						<xsl:value-of select="."/>
					</xsl:element>
				</xsl:for-each>
			</body>
		</html>
	</xsl:template>

</xsl:stylesheet> 