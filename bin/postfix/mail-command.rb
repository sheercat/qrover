#!/usr/bin/env ruby
# -*- coding: utf-8 -*-
require 'rubygems'
require 'mail'
require 'pp'
require 'optparse'
require 'net/http'

OPTS = {}

opt = OptionParser.new

opt.on('-m=') {|v| OPTS[:m] = v }
opt.on('-c=') {|v| OPTS[:c] = v }
opt.on('-e=') {|v| OPTS[:e] = v }
opt.on('-s=') {|v| OPTS[:s] = v }

opt.parse!(ARGV)

pp OPTS

message = $stdin.read
mail = Mail.new(message)

# p '--------------------------------------------------------------------------------'
# pp mail
#
# p '--------------------------------------------------------------------------------'
# p mail.from
#
# p '--------------------------------------------------------------------------------'
# p mail.to

## require 'mechanize'
##
## agent = Mechanize.new
## page = agent.get('http://localhost:3000/api/insert_email_bulky')
##
## p page.code
##
## form = page.form_with(:name => 'insert')
## form.sender = OPTS[:s]
## form.ext = OPTS[:e]
## form.mbox = OPTS[:m]
## form.cmd = OPTS[:c]
## form.from = mail.from
## form.to = mail.to
## # p "[" + mail.charset + "]"
## # p "[" + mail.encoded + "]"
## # p "[" + mail.decoded + "]"
## # p "[" + mail.body.decoded.encode("UTF-8", mail.charset) + "]"
## # p "[" + message.encode("UTF-8", mail.charset) + "]"
##
## form.body = message.encode("UTF-8", mail.charset)
##
## agent.submit(form)
##
## p agent.page.code
##


# タイムアウト設定
time_out = 10

uri = URI.parse("http://localhost:3001/api/insert_email")

Net::HTTP.start(uri.host, uri.port){|http|
  # リクエストインスタンス生成
  request = Net::HTTP::Post.new(uri.path)
  request.basic_auth 'diver','deai114'
  request["user-agent"] = "Ruby/#{RUBY_VERSION} mail-command for qrover"
  request.set_form_data("email" => OPTS[:s],
                        "code" => OPTS[:e],
                        "mbox" => OPTS[:m],
                        "type" => OPTS[:c],
                        "from" => mail.from,
                        "to" => mail.to,
                        "body"=> message.encode("UTF-8", mail.charset),
                        )
  # time out
  http.open_timeout = time_out
  http.read_timeout = time_out

  # 送信
  response = http.request(request)

  # p "====RESULT(#{uri.host})========"
  p "==> "+response.body
  # pp response
}

exit(0)
