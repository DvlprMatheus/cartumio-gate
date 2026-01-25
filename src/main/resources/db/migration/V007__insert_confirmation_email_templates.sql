-- Migration V007: Insert confirmation email templates
-- Description: Insert the confirmation email templates for pt-BR and en-US languages
-- Author: Matheus Cruz

INSERT INTO email_templates (code, language, subject, body, active, created_at) VALUES 
('confirmation-template', 'pt-BR', 'Confirme seu e-mail - Cartumio', 
'<!DOCTYPE html>
<html lang="pt-BR">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <style>
        body {
            font-family: Arial, sans-serif;
            margin: 0;
            padding: 0;
            background-color: #f5f5f5;
            color: #333;
        }

        .email-container {
            width: 100%;
            max-width: 600px;
            margin: 0 auto;
            background-color: #fff;
            padding: 20px;
            border-radius: 8px;
            box-shadow: 0 4px 6px rgba(0, 0, 0, 0.1);
        }

        .header {
            text-align: center;
            background-color: #000;
            padding: 20px;
            border-radius: 8px 8px 0 0;
        }

        .header h1 {
            color: #fff;
            font-size: 36px;
            font-family: ''Helvetica Neue'', sans-serif;
            letter-spacing: 2px;
            margin: 0;
        }

        .body-content {
            padding: 20px;
        }

        .body-content h2 {
            font-size: 18px;
            color: #333;
            margin-bottom: 10px;
        }

        .body-content p {
            font-size: 16px;
            color: #666;
            line-height: 1.5;
            margin-bottom: 30px;
        }

        .cta-button {
            display: inline-block;
            padding: 12px 30px;
            background-color: #000;
            color: #fff;
            text-decoration: none;
            font-size: 16px;
            border-radius: 5px;
            text-transform: uppercase;
            margin: 0 auto 20px auto;
            text-align: center;
        }

        .cta-link {
            display: inline-block;
            font-size: 14px;
            color: #333;
            background-color: #f4f4f4;
            border: 1px solid #dcdcdc;
            border-radius: 4px;
            padding: 8px 12px;
            word-wrap: break-word;
            text-decoration: none;
            font-family: "Courier New", Courier, monospace;
            margin-top: 20px;
            text-align: center;
        }

        .cta-link:hover {
            background-color: #eaeaea;
        }

        .footer {
            text-align: center;
            font-size: 12px;
            color: #999;
            margin-top: 30px;
        }

        .footer p {
            margin: 5px 0;
        }
    </style>
</head>
<body>
    <div class="email-container">
        <div class="header">
            <h1>Cartumio</h1>
        </div>

        <div class="body-content">
            <h2>Olá, {{fullName}}!</h2>
            <p>Para confirmar seu registro na fila de espera, por favor, confirme seu e-mail clicando no botão abaixo:</p>
            <div style="text-align: center;">
                <a href="{{url}}" class="cta-button">Confirmar E-mail</a>
            </div>
            <p>Ou copie e cole o seguinte link no seu navegador:</p>
            <div style="text-align: center;">
                <a href="{{url}}" class="cta-link">{{url}}</a>
            </div>
        </div>

        <div class="footer">
            <p>Este link expira em 24 horas.</p>
            <p>Se você não fez essa solicitação, ignore este e-mail.</p>
        </div>
    </div>
</body>
</html>', 
TRUE, CURRENT_TIMESTAMP),

('confirmation-template', 'en-US', 'Confirm your email - Cartumio', 
'<!DOCTYPE html>
<html lang="en-US">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <style>
        body {
            font-family: Arial, sans-serif;
            margin: 0;
            padding: 0;
            background-color: #f5f5f5;
            color: #333;
        }

        .email-container {
            width: 100%;
            max-width: 600px;
            margin: 0 auto;
            background-color: #fff;
            padding: 20px;
            border-radius: 8px;
            box-shadow: 0 4px 6px rgba(0, 0, 0, 0.1);
        }

        .header {
            text-align: center;
            background-color: #000;
            padding: 20px;
            border-radius: 8px 8px 0 0;
        }

        .header h1 {
            color: #fff;
            font-size: 36px;
            font-family: ''Helvetica Neue'', sans-serif;
            letter-spacing: 2px;
            margin: 0;
        }

        .body-content {
            padding: 20px;
        }

        .body-content h2 {
            font-size: 18px;
            color: #333;
            margin-bottom: 10px;
        }

        .body-content p {
            font-size: 16px;
            color: #666;
            line-height: 1.5;
            margin-bottom: 30px;
        }

        .cta-button {
            display: inline-block;
            padding: 12px 30px;
            background-color: #000;
            color: #fff;
            text-decoration: none;
            font-size: 16px;
            border-radius: 5px;
            text-transform: uppercase;
            margin: 0 auto 20px auto;
            text-align: center;
        }

        .cta-link {
            display: inline-block;
            font-size: 14px;
            color: #333;
            background-color: #f4f4f4;
            border: 1px solid #dcdcdc;
            border-radius: 4px;
            padding: 8px 12px;
            word-wrap: break-word;
            text-decoration: none;
            font-family: "Courier New", Courier, monospace;
            margin-top: 20px;
            text-align: center;
        }

        .cta-link:hover {
            background-color: #eaeaea;
        }

        .footer {
            text-align: center;
            font-size: 12px;
            color: #999;
            margin-top: 30px;
        }

        .footer p {
            margin: 5px 0;
        }
    </style>
</head>
<body>
    <div class="email-container">
        <div class="header">
            <h1>Cartumio</h1>
        </div>

        <div class="body-content">
            <h2>Hello, {{fullName}}!</h2>
            <p>To confirm your registration on the waiting list, please confirm your email by clicking the button below:</p>
            <div style="text-align: center;">
                <a href="{{url}}" class="cta-button">Confirm Email</a>
            </div>
            <p>Or copy and paste the following link into your browser:</p>
            <div style="text-align: center;">
                <a href="{{url}}" class="cta-link">{{url}}</a>
            </div>
        </div>

        <div class="footer">
            <p>This link will expire in 24 hours.</p>
            <p>If you did not make this request, please ignore this email.</p>
        </div>
    </div>
</body>
</html>', 
TRUE, CURRENT_TIMESTAMP);
