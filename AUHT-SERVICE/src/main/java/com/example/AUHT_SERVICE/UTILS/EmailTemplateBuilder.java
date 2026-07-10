package com.example.AUHT_SERVICE.UTILS;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class EmailTemplateBuilder {

    private static final String LOGO_URL = "https://res.cloudinary.com/dgrdonnsk/image/upload/v1773947202/imagen02_r0mqdt.webp";
    private static final String ACCENT_COLOR = "#FF007F";
    private static final String BG_COLOR = "#f6f9fc";
    private static final String CARD_BG = "#ffffff";
    private static final String TEXT_PRIMARY = "#1a202c";
    private static final String TEXT_SECONDARY = "#718096";

    public static String buildOtpTemplate(String title, String message, String otpCode) {
        return """
            <div style="background-color: %s; padding: 40px 0; font-family: 'Segoe UI', Roboto, Helvetica, Arial, sans-serif;">
                <table align="center" border="0" cellpadding="0" cellspacing="0" width="100%%" style="max-width: 450px; background-color: %s; border-radius: 0px; border: 1px solid #e1e8ed; overflow: hidden;">
                    <tr>
                        <td style="padding: 40px; text-align: center;">
                            <img src="%s" alt="Logo" width="100" style="margin-bottom: 25px; display: block; margin-left: auto; margin-right: auto;">
                            <span style="color: %s; font-weight: 800; font-size: 11px; text-transform: uppercase; letter-spacing: 3px; display: block; margin-bottom: 15px;">
                                %s
                            </span>
                            <h2 style="color: %s; font-size: 22px; font-weight: 700; margin: 0 0 15px 0;">
                                Verifica tu identidad
                            </h2>
                            <p style="color: %s; font-size: 15px; line-height: 1.6; margin-bottom: 25px;">
                                %s Este código es válido por <b>15 minutos</b>.
                            </p>
                            <div style="background-color: #f8fafc; border: 2px dashed %s; padding: 20px; border-radius: 0px; display: inline-block; width: 80%%;">
                                <span style="font-family: 'Courier New', monospace; font-size: 32px; font-weight: 700; color: #1e293b; letter-spacing: 8px;">
                                    %s
                                </span>
                            </div>
                            <p style="color: #a0aec0; font-size: 12px; margin-top: 30px;">
                                Si no solicitaste este código, por favor ignora este mensaje o contacta a soporte.
                            </p>
                        </td>
                    </tr>
                    <tr>
                        <td style="background-color: #fcfdfe; padding: 20px; text-align: center; border-top: 1px solid #edf2f7;">
                            <p style="margin: 0; color: %s; font-size: 11px;">
                                &copy; 2026 EventPeru &bull; Lima, Perú
                            </p>
                        </td>
                    </tr>
                </table>
            </div>
            """.formatted(
                BG_COLOR, CARD_BG, LOGO_URL,
                ACCENT_COLOR, title, TEXT_PRIMARY,
                TEXT_SECONDARY, message, ACCENT_COLOR,
                otpCode, TEXT_SECONDARY
        );
    }

    // ← AGREGAR ESTE MÉTODO
    public static String buildResetPasswordTemplate(String resetLink) {
        return """
            <div style="background-color: %s; padding: 40px 0; font-family: 'Segoe UI', Roboto, Helvetica, Arial, sans-serif;">
                <table align="center" border="0" cellpadding="0" cellspacing="0" width="100%%" style="max-width: 450px; background-color: %s; border-radius: 0px; border: 1px solid #e1e8ed; overflow: hidden;">
                    <tr>
                        <td style="padding: 40px; text-align: center;">
                            <div style="margin-bottom: 25px;">
                                <span style="font-size: 22px; font-weight: 800; color: #080808;">Event</span>
                                <span style="font-size: 22px; font-weight: 800; color: #00cc52;">Peru</span>
                            </div>
                            <span style="color: #00cc52; font-weight: 800; font-size: 11px; text-transform: uppercase; letter-spacing: 3px; display: block; margin-bottom: 12px;">
                                SEGURIDAD
                            </span>
                            <h2 style="color: #080808; font-size: 22px; font-weight: 700; margin: 0 0 12px 0;">
                                Recupera tu contraseña
                            </h2>
                            <p style="color: #718096; font-size: 14px; line-height: 1.6; margin-bottom: 28px;">
                                Has solicitado restablecer tu contraseña. Haz clic en el botón para continuar.
                                Este enlace expira en <b>1 hora</b>.
                            </p>
                            <a href="%s" style="display: inline-block; background-color: #00cc52; color: #080808; font-size: 15px; font-weight: 700; text-decoration: none; padding: 14px 36px; border-radius: 8px; margin-bottom: 24px;">
                                Restablecer contraseña →
                            </a>
                            <p style="color: #a0aec0; font-size: 12px; margin-top: 8px; margin-bottom: 0;">
                                Si el botón no funciona, copia este enlace:
                            </p>
                            <p style="font-family: 'Courier New', monospace; font-size: 11px; color: #718096; word-break: break-all; margin-top: 6px;">
                                %s
                            </p>
                            <p style="color: #a0aec0; font-size: 12px; margin-top: 24px; border-top: 1px solid #edf2f7; padding-top: 20px;">
                                Si no solicitaste este cambio, ignora este mensaje.
                            </p>
                        </td>
                    </tr>
                    <tr>
                        <td style="background-color: #f8fafb; padding: 20px; text-align: center; border-top: 1px solid #edf2f7;">
                            <p style="margin: 0; color: #a0aec0; font-size: 11px;">
                                &copy; 2026 EventPeru &bull; Lima, Perú
                            </p>
                        </td>
                    </tr>
                </table>
            </div>
            """.formatted(BG_COLOR, CARD_BG, resetLink, resetLink);
    }
}